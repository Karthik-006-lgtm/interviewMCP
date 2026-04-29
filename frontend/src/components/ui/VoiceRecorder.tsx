import { useEffect, useRef, useState } from "react";
import { interviewApi } from "../../api/client";
import type { AudioProcessingResult } from "../../types";

type SpeechRecognitionConstructor = new () => BrowserSpeechRecognition;

interface BrowserSpeechRecognition extends EventTarget {
  continuous: boolean;
  interimResults: boolean;
  lang: string;
  onresult: ((event: SpeechRecognitionEventLike) => void) | null;
  onerror: ((event: { error: string }) => void) | null;
  start: () => void;
  stop: () => void;
  abort: () => void;
}

interface SpeechRecognitionEventLike {
  results: ArrayLike<ArrayLike<{ transcript: string }>>;
}

declare global {
  interface Window {
    SpeechRecognition?: SpeechRecognitionConstructor;
    webkitSpeechRecognition?: SpeechRecognitionConstructor;
  }
}

export interface VideoRecordingResult {
  blob: Blob;
  url: string;
}

interface VoiceRecorderProps {
  onRecorded: (result: AudioProcessingResult) => void;
  onProcessingChange?: (processing: boolean) => void;
  onRecordingStart?: () => void;
  onRecordingStop?: () => void;
  cameraEnabled?: boolean;
  onVideoRecorded?: (result: VideoRecordingResult) => void;
}

export function VoiceRecorder({
  onRecorded,
  onProcessingChange,
  onRecordingStart,
  onRecordingStop,
  cameraEnabled,
  onVideoRecorded,
}: VoiceRecorderProps) {
  const recorderRef = useRef<MediaRecorder | null>(null);
  const audioStreamRef = useRef<MediaStream | null>(null);
  const recognitionRef = useRef<BrowserSpeechRecognition | null>(null);
  const chunksRef = useRef<BlobPart[]>([]);
  const startedAtRef = useRef<number | null>(null);
  const transcriptRef = useRef("");

  // Video recording refs
  const videoRecorderRef = useRef<MediaRecorder | null>(null);
  const videoStreamRef = useRef<MediaStream | null>(null);
  const videoChunksRef = useRef<BlobPart[]>([]);
  const videoPreviewRef = useRef<HTMLVideoElement | null>(null);

  const [recording, setRecording] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [previewUrl, setPreviewUrl] = useState("");
  const [videoPreviewUrl, setVideoPreviewUrl] = useState("");
  const [message, setMessage] = useState("");
  const [transcriptHint, setTranscriptHint] = useState("");
  const [analysis, setAnalysis] = useState<AudioProcessingResult | null>(null);

  useEffect(() => {
    return () => {
      stopAllMedia();
    };
  }, []);

  useEffect(() => {
    return () => {
      if (previewUrl) URL.revokeObjectURL(previewUrl);
    };
  }, [previewUrl]);

  useEffect(() => {
    return () => {
      if (videoPreviewUrl) URL.revokeObjectURL(videoPreviewUrl);
    };
  }, [videoPreviewUrl]);

  const startRecording = async () => {
    try {
      if (typeof MediaRecorder === "undefined") {
        setMessage("This browser does not support in-page audio recording yet. You can still type your answer.");
        return;
      }

      // Get audio stream
      const audioStream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const audioMimeType = resolveAudioMimeType();
      const audioRecorder = audioMimeType
        ? new MediaRecorder(audioStream, { mimeType: audioMimeType })
        : new MediaRecorder(audioStream);

      audioStreamRef.current = audioStream;
      recorderRef.current = audioRecorder;
      chunksRef.current = [];
      startedAtRef.current = Date.now();
      setMessage("");
      setAnalysis(null);
      setTranscriptHint("");
      transcriptRef.current = "";

      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
        setPreviewUrl("");
      }
      if (videoPreviewUrl) {
        URL.revokeObjectURL(videoPreviewUrl);
        setVideoPreviewUrl("");
      }

      audioRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) chunksRef.current.push(event.data);
      };

      audioRecorder.onstop = async () => {
        const blob = new Blob(chunksRef.current, { type: audioRecorder.mimeType || "audio/webm" });
        const nextPreviewUrl = URL.createObjectURL(blob);
        setPreviewUrl(nextPreviewUrl);
        stopAudioMedia();
        await uploadRecording(blob, audioRecorder.mimeType || "audio/webm");
      };

      // Start video recording if camera mode is enabled
      if (cameraEnabled) {
        await startVideoRecording();
      }

      startSpeechRecognition();
      audioRecorder.start();
      setRecording(true);
      onRecordingStart?.();

      if (cameraEnabled) {
        setMessage("Recording audio + video. Speak naturally and stop when your answer is complete.");
      } else {
        setMessage("Recording in progress. Speak naturally and stop when your answer is complete.");
      }
    } catch {
      setMessage("Microphone access was blocked. You can still type your answer manually.");
    }
  };

  const startVideoRecording = async () => {
    try {
      const videoStream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: "user", width: { ideal: 640 }, height: { ideal: 480 } },
        audio: false,
      });

      videoStreamRef.current = videoStream;
      videoChunksRef.current = [];

      // Show live preview
      if (videoPreviewRef.current) {
        videoPreviewRef.current.srcObject = videoStream;
        await videoPreviewRef.current.play().catch(() => undefined);
      }

      const videoMimeType = resolveVideoMimeType();
      const videoRecorder = videoMimeType
        ? new MediaRecorder(videoStream, { mimeType: videoMimeType })
        : new MediaRecorder(videoStream);

      videoRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) videoChunksRef.current.push(event.data);
      };

      videoRecorder.onstop = () => {
        const videoBlob = new Blob(videoChunksRef.current, {
          type: videoRecorder.mimeType || "video/webm",
        });
        const url = URL.createObjectURL(videoBlob);
        setVideoPreviewUrl(url);
        onVideoRecorded?.({ blob: videoBlob, url });
        stopVideoMedia();
      };

      videoRecorderRef.current = videoRecorder;
      videoRecorder.start();
    } catch {
      // Camera not available — continue with audio only
    }
  };

  const stopRecording = () => {
    recognitionRef.current?.stop();
    recorderRef.current?.stop();
    videoRecorderRef.current?.stop();
    setRecording(false);
    onRecordingStop?.();
  };

  const uploadRecording = async (blob: Blob, mimeType: string) => {
    setUploading(true);
    onProcessingChange?.(true);
    setMessage("Uploading audio and analyzing speech...");

    try {
      const durationMs = startedAtRef.current ? Date.now() - startedAtRef.current : undefined;
      const result = await interviewApi.uploadAudio({
        file: blob,
        fileName: `voice-answer.${extensionForMimeType(mimeType)}`,
        transcriptHint: transcriptRef.current,
        durationMs,
      });
      setAnalysis(result);
      onRecorded(result);
      if (result.transcript.trim()) {
        setMessage("Speech analysis is ready. Review the transcript and scorecard before you submit.");
      } else {
        setMessage(
          "Speech analysis is ready but no transcript was captured. Please type your answer in the text box above before submitting."
        );
      }
    } catch {
      setMessage("Audio upload failed. Your typed answer still works, and you can try recording again.");
    } finally {
      setUploading(false);
      onProcessingChange?.(false);
    }
  };

  const startSpeechRecognition = () => {
    const Recognition = window.SpeechRecognition ?? window.webkitSpeechRecognition;
    if (!Recognition) return;

    const recognition = new Recognition();
    recognition.lang = "en-US";
    recognition.continuous = true;
    recognition.interimResults = true;
    recognition.onresult = (event) => {
      const transcript = Array.from(event.results)
        .map((result) => result[0]?.transcript ?? "")
        .join(" ")
        .trim();
      setTranscriptHint(transcript);
      transcriptRef.current = transcript;
    };
    recognition.onerror = () => {
      /* live transcript unavailable — audio recording continues */
    };

    recognitionRef.current = recognition;
    recognition.start();
  };

  const stopAudioMedia = () => {
    recognitionRef.current?.abort();
    recognitionRef.current = null;
    audioStreamRef.current?.getTracks().forEach((track) => track.stop());
    audioStreamRef.current = null;
    recorderRef.current = null;
  };

  const stopVideoMedia = () => {
    if (videoPreviewRef.current) videoPreviewRef.current.srcObject = null;
    videoStreamRef.current?.getTracks().forEach((track) => track.stop());
    videoStreamRef.current = null;
    videoRecorderRef.current = null;
  };

  const stopAllMedia = () => {
    stopAudioMedia();
    stopVideoMedia();
  };

  return (
    <div className="app-surface rounded-[1.5rem] p-4">
      {/* Live camera preview while recording */}
      {cameraEnabled && recording ? (
        <div className="mb-4 overflow-hidden rounded-[1.25rem] border border-cyan-500/30 bg-black/35">
          <video
            ref={videoPreviewRef}
            className="aspect-video w-full object-cover"
            autoPlay
            muted
            playsInline
          />
          <div className="flex items-center gap-2 px-3 py-2 text-xs text-rose-400">
            <span className="inline-block h-2 w-2 animate-pulse rounded-full bg-rose-500" />
            Recording video + audio
          </div>
        </div>
      ) : null}

      <div className="flex flex-wrap items-center gap-3">
        {!recording ? (
          <button
            type="button"
            onClick={startRecording}
            disabled={uploading}
            className="app-button-primary rounded-full px-4 py-2 text-sm font-medium transition disabled:opacity-60"
          >
            {uploading ? "Analyzing..." : cameraEnabled ? "Start recording (audio + video)" : "Record voice answer"}
          </button>
        ) : (
          <button
            type="button"
            onClick={stopRecording}
            className="rounded-full bg-rose-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-rose-500"
          >
            Stop recording
          </button>
        )}
        <span className="text-sm text-white/64">
          {cameraEnabled
            ? "Camera and microphone will record together. Video is used for visual metrics."
            : "Browser speech recognition fills the transcript when available, and the backend scores delivery after upload."}
        </span>
      </div>

      {message ? <p className="mt-3 text-sm text-white/68">{message}</p> : null}
      {previewUrl ? <audio className="mt-4 w-full" src={previewUrl} controls /> : null}

      {/* Video playback after recording */}
      {videoPreviewUrl && !recording ? (
        <div className="mt-4">
          <p className="mb-2 text-xs font-semibold uppercase tracking-[0.25em] text-white/48">
            Recorded video
          </p>
          <video
            className="aspect-video w-full rounded-[1.25rem] border border-white/10 bg-black/35 object-cover"
            src={videoPreviewUrl}
            controls
            playsInline
          />
        </div>
      ) : null}

      {transcriptHint ? (
        <div className="app-surface-strong mt-4 rounded-[1.25rem] p-4">
          <p className="text-xs font-semibold uppercase tracking-[0.25em] text-white/48">Transcript draft</p>
          <p className="mt-2 text-sm leading-7 text-white/78">{transcriptHint}</p>
        </div>
      ) : null}

      {analysis ? (
        <div className="mt-4 grid gap-4 lg:grid-cols-2">
          <div className="app-surface-strong rounded-[1.25rem] p-4">
            <p className="text-xs font-semibold uppercase tracking-[0.25em] text-white/48">Speech scorecard</p>
            <div className="mt-3 space-y-2 text-sm text-white/78">
              <p>Confidence: {Math.round(analysis.confidenceScore)}%</p>
              <p>Fluency: {Math.round(analysis.fluencyScore)}%</p>
              <p>Clarity: {Math.round(analysis.clarityScore)}%</p>
              <p>Emotion: {analysis.emotionSignal}</p>
            </div>
          </div>
          <div className="app-surface-strong rounded-[1.25rem] p-4 text-sm text-white/78">
            <p>
              <span className="font-semibold text-cyan-200">Tone:</span> {analysis.toneFeedback}
            </p>
            <p className="mt-2">
              <span className="font-semibold text-cyan-200">Pronunciation:</span> {analysis.pronunciationFeedback}
            </p>
            <p className="mt-2">
              <span className="font-semibold text-cyan-200">Fluency:</span> {analysis.fluencyFeedback}
            </p>
          </div>
        </div>
      ) : null}
    </div>
  );
}

function resolveAudioMimeType() {
  if (typeof MediaRecorder === "undefined") return undefined;
  const candidates = ["audio/webm;codecs=opus", "audio/webm", "audio/mp4"];
  return candidates.find((c) => MediaRecorder.isTypeSupported(c));
}

function resolveVideoMimeType() {
  if (typeof MediaRecorder === "undefined") return undefined;
  const candidates = ["video/webm;codecs=vp9,opus", "video/webm;codecs=vp8,opus", "video/webm", "video/mp4"];
  return candidates.find((c) => MediaRecorder.isTypeSupported(c));
}

function extensionForMimeType(mimeType: string) {
  if (mimeType.includes("mp4")) return "m4a";
  return "webm";
}
