import { useEffect, useRef, useState } from "react";

interface FaceBoundsLike {
  width?: number;
}

interface FaceDetectionLike {
  boundingBox?: FaceBoundsLike;
}

interface FaceDetectorLike {
  detect: (input: CanvasImageSource) => Promise<FaceDetectionLike[]>;
}

interface FaceDetectorConstructor {
  new (options?: { fastMode?: boolean; maxDetectedFaces?: number }): FaceDetectorLike;
}

declare global {
  interface Window {
    FaceDetector?: FaceDetectorConstructor;
  }
}

interface CameraPresencePanelProps {
  enabled: boolean;
  onSignalChange?: (signal: {
    presence: string;
    eyeContact: string;
    confidence: string;
    nervousness: string;
  }) => void;
}

export function CameraPresencePanel({ enabled, onSignalChange }: CameraPresencePanelProps) {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const monitorHandleRef = useRef<number | null>(null);
  const previousFaceWidthRef = useRef<number | null>(null);
  const missingFramesRef = useRef(0);

  const [status, setStatus] = useState("Camera mode is off for this session.");
  const [presenceSignal, setPresenceSignal] = useState("Voice-based emotion analysis only.");
  const [eyeContactProxy, setEyeContactProxy] = useState("Enable camera mode to add visual presence checks.");
  const [confidenceSignal, setConfidenceSignal] = useState("Visual confidence scoring is inactive.");
  const [nervousnessSignal, setNervousnessSignal] = useState("Visual nervousness scoring is inactive.");

  const publishSignals = (
    presence: string,
    eyeContact: string,
    confidence: string,
    nervousness: string
  ) => {
    onSignalChange?.({
      presence,
      eyeContact,
      confidence,
      nervousness
    });
  };

  useEffect(() => {
    if (!enabled) {
      stopCamera();
      setStatus("Camera mode is off for this session.");
      setPresenceSignal("Voice-based emotion analysis only.");
      setEyeContactProxy("Enable camera mode to add visual presence checks.");
      setConfidenceSignal("Visual confidence scoring is inactive.");
      setNervousnessSignal("Visual nervousness scoring is inactive.");
      publishSignals(
        "Voice-based emotion analysis only.",
        "Enable camera mode to add visual presence checks.",
        "Visual confidence scoring is inactive.",
        "Visual nervousness scoring is inactive."
      );
      return;
    }

    let cancelled = false;

    const startCamera = async () => {
      if (!navigator.mediaDevices?.getUserMedia) {
        setStatus("Camera capture is not supported in this browser.");
        setPresenceSignal("Visual presence checks are unavailable.");
        setEyeContactProxy("Voice emotion analysis remains active.");
        setConfidenceSignal("Visual confidence scoring is unavailable.");
        setNervousnessSignal("Visual nervousness scoring is unavailable.");
        publishSignals(
          "Visual presence checks are unavailable.",
          "Voice emotion analysis remains active.",
          "Visual confidence scoring is unavailable.",
          "Visual nervousness scoring is unavailable."
        );
        return;
      }

      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "user" },
          audio: false
        });

        if (cancelled) {
          stream.getTracks().forEach((track) => track.stop());
          return;
        }

        streamRef.current = stream;

        if (videoRef.current) {
          videoRef.current.srcObject = stream;
          await videoRef.current.play().catch(() => undefined);
        }

        setStatus("Camera feed is active for this interview.");

        if (!window.FaceDetector) {
          setPresenceSignal("Camera feed is active.");
          setEyeContactProxy("This browser does not expose face detection, so visual analysis stays in readiness mode.");
          setConfidenceSignal("Camera is on, but browser-side confidence scoring is limited.");
          setNervousnessSignal("Use the voice scorecard for nervousness signals in this browser.");
          publishSignals(
            "Camera feed is active.",
            "This browser does not expose face detection, so visual analysis stays in readiness mode.",
            "Camera is on, but browser-side confidence scoring is limited.",
            "Use the voice scorecard for nervousness signals in this browser."
          );
          return;
        }

        const detector = new window.FaceDetector({ fastMode: true, maxDetectedFaces: 1 });
        monitorHandleRef.current = window.setInterval(async () => {
          if (!videoRef.current || videoRef.current.readyState < 2) {
            return;
          }

          try {
            const faces = await detector.detect(videoRef.current);
            if (!faces.length) {
              missingFramesRef.current += 1;
              setPresenceSignal("No face detected in frame.");
              setEyeContactProxy("Move back into frame to strengthen visual confidence signals.");
              setConfidenceSignal("Confidence read is unavailable while you are out of frame.");
              setNervousnessSignal(
                missingFramesRef.current >= 2
                  ? "Frequent frame breaks detected. This can read like nervous energy in a mock interview."
                  : "Stay in frame for more stable visual analysis."
              );
              publishSignals(
                "No face detected in frame.",
                "Move back into frame to strengthen visual confidence signals.",
                "Confidence read is unavailable while you are out of frame.",
                missingFramesRef.current >= 2
                  ? "Frequent frame breaks detected. This can read like nervous energy in a mock interview."
                  : "Stay in frame for more stable visual analysis."
              );
              return;
            }

            const faceWidth = faces[0].boundingBox?.width || 0;
            const previousWidth = previousFaceWidthRef.current;
            const widthDelta = previousWidth == null ? 0 : Math.abs(previousWidth - faceWidth);
            previousFaceWidthRef.current = faceWidth;
            missingFramesRef.current = 0;

            const nextPresence = faceWidth >= 105 ? "Face detected and tracked consistently." : "Face detected, but the frame is distant.";
            const nextEyeContact =
              faceWidth >= 118
                ? "Eye-contact proxy looks steady."
                : "Move a little closer and center yourself to strengthen eye-contact cues.";
            const nextConfidence =
              faceWidth >= 118 && widthDelta <= 8
                ? "Visual confidence appears steady and composed."
                : faceWidth >= 100
                  ? "Visual confidence is moderate; reduce movement and keep a stable posture."
                  : "Visual confidence looks tentative because the framing is weak.";
            const nextNervousness =
              widthDelta > 20
                ? "Noticeable movement between samples suggests possible nervousness."
                : widthDelta > 10
                  ? "Small visual instability detected. Try keeping your head position steadier."
                  : "Visual nervousness cues look low right now.";

            setPresenceSignal(nextPresence);
            setEyeContactProxy(nextEyeContact);
            setConfidenceSignal(nextConfidence);
            setNervousnessSignal(nextNervousness);
            publishSignals(nextPresence, nextEyeContact, nextConfidence, nextNervousness);
          } catch {
            setPresenceSignal("Visual presence sampling is temporarily unavailable.");
            setEyeContactProxy("Voice emotion analysis remains active.");
            setConfidenceSignal("Visual confidence scoring is temporarily unavailable.");
            setNervousnessSignal("Visual nervousness scoring is temporarily unavailable.");
            publishSignals(
              "Visual presence sampling is temporarily unavailable.",
              "Voice emotion analysis remains active.",
              "Visual confidence scoring is temporarily unavailable.",
              "Visual nervousness scoring is temporarily unavailable."
            );
          }
        }, 2500);
      } catch {
        setStatus("Camera access was blocked or unavailable.");
        setPresenceSignal("Visual presence checks could not start.");
        setEyeContactProxy("Voice emotion analysis remains active.");
        setConfidenceSignal("Visual confidence scoring could not start.");
        setNervousnessSignal("Visual nervousness scoring could not start.");
        publishSignals(
          "Visual presence checks could not start.",
          "Voice emotion analysis remains active.",
          "Visual confidence scoring could not start.",
          "Visual nervousness scoring could not start."
        );
      }
    };

    startCamera();

    return () => {
      cancelled = true;
      stopCamera();
    };
  }, [enabled]);

  const stopCamera = () => {
    if (monitorHandleRef.current !== null) {
      window.clearInterval(monitorHandleRef.current);
      monitorHandleRef.current = null;
    }
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
    }
    if (videoRef.current) {
      videoRef.current.srcObject = null;
    }
  };

  return (
    <div className="app-surface rounded-[1.5rem] p-5 text-sm text-white/78">
      <p className="font-semibold text-cyan-200">Camera presence mode</p>
      <p className="mt-2 leading-7">{status}</p>
      <div className="mt-4 overflow-hidden rounded-[1.25rem] border border-white/10 bg-black/35">
        <video ref={videoRef} className="aspect-video w-full object-cover" autoPlay muted playsInline />
      </div>
      <div className="mt-4 space-y-2">
        <p>
          <span className="font-semibold text-white">Presence:</span> {presenceSignal}
        </p>
        <p>
          <span className="font-semibold text-white">Eye-contact proxy:</span> {eyeContactProxy}
        </p>
        <p>
          <span className="font-semibold text-white">Confidence cue:</span> {confidenceSignal}
        </p>
        <p>
          <span className="font-semibold text-white">Nervousness cue:</span> {nervousnessSignal}
        </p>
      </div>
    </div>
  );
}
