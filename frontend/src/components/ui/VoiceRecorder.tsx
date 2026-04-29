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

interface VoiceRecorderProps {
  onRecorded: (result: AudioProcessingResult) => void;
  onProcessingChange?: (processing: boolean) => void;
}

export function VoiceRecorder({ onRecorded, onProcessingChange }: VoiceRecorderProps) {
  const recorderRef = useRef<MediaRecorder | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const recognitionRef = useRef<BrowserSpeechRecognition | null>(null);
  const chunksRef = useRef<BlobPart[]>([]);
  const startedAtRef = useRef<number | null>(null);
  const transcriptRef = useRef("");

  const [recording, setRecording] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [previewUrl, setPreviewUrl] = useState("");
  const [message, setMessage] = useState("");
  const [transcriptHint, setTranscriptHint] = useState("");
  const [analysis, setAnalysis] = useState<AudioProcessingResult | null>(null);

  useEffect(() => {
    return () => {
      stopMedia();
    };
  }, []);

  useEffect(() => {
    return () => {
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  const startRecording = async () => {
    try {
      if (typeof MediaRecorder === "undefined") {
        setMessage("This browser does not support in-page audio recording yet. You can still type your answer.");
        return;
      }
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const mimeType = resolveMimeType();
      const mediaRecorder = mimeType ? new MediaRecorder(stream, { mimeType }) : new MediaRecorder(stream);

      streamRef.current = stream;
      recorderRef.current = mediaRecorder;
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

      mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          chunksRef.current.push(event.data);
        }
      };

      mediaRecorder.onstop = async () => {
        const blob = new Blob(chunksRef.current, { type: mediaRecorder.mimeType || "audio/webm" });
        const nextPreviewUrl = URL.createObjectURL(blob);
        setPreviewUrl(nextPreviewUrl);
        stopMedia();
        await uploadRecording(blob, mediaRecorder.mimeType || "audio/webm");
      };

      startSpeechRecognition();
      mediaRecorder.start();
      setRecording(true);
      setMessage("Recording in progress. Speak naturally and stop when your answer is complete.");
    } catch {
      setMessage("Microphone access was blocked. You can still type your answer manually.");
    }
  };

  const stopRecording = () => {
    recognitionRef.current?.stop();
    recorderRef.current?.stop();
    setRecording(false);
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
        durationMs
      });
      setAnalysis(result);
      onRecorded(result);
      setMessage("Speech analysis is ready. Review the transcript and scorecard before you submit.");
    } catch {
      setMessage("Audio upload failed. Your typed answer still works, and you can try recording again.");
    } finally {
      setUploading(false);
      onProcessingChange?.(false);
    }
  };

  const startSpeechRecognition = () => {
    const Recognition = window.SpeechRecognition ?? window.webkitSpeechRecognition;
    if (!Recognition) {
      return;
    }

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
      setMessage("Audio recording will continue, but live transcript capture is not available in this browser.");
    };

    recognitionRef.current = recognition;
    recognition.start();
  };

  const stopMedia = () => {
    recognitionRef.current?.abort();
    recognitionRef.current = null;
    streamRef.current?.getTracks().forEach((track) => track.stop());
    streamRef.current = null;
    recorderRef.current = null;
  };

  return (
    <div className="app-surface rounded-[1.5rem] p-4">
      <div className="flex flex-wrap items-center gap-3">
        {!recording ? (
          <button
            type="button"
            onClick={startRecording}
            disabled={uploading}
            className="app-button-primary rounded-full px-4 py-2 text-sm font-medium transition disabled:opacity-60"
          >
            {uploading ? "Analyzing..." : "Record voice answer"}
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
          Browser speech recognition fills the transcript when available, and the backend scores delivery after upload.
        </span>
      </div>

      {message ? <p className="mt-3 text-sm text-white/68">{message}</p> : null}
      {previewUrl ? <audio className="mt-4 w-full" src={previewUrl} controls /> : null}

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

function resolveMimeType() {
  if (typeof MediaRecorder === "undefined") {
    return undefined;
  }
  const supportedMimeTypes = ["audio/webm;codecs=opus", "audio/webm", "audio/mp4"];
  return supportedMimeTypes.find((candidate) => MediaRecorder.isTypeSupported(candidate));
}

function extensionForMimeType(mimeType: string) {
  if (mimeType.includes("mp4")) {
    return "m4a";
  }
  return "webm";
}

