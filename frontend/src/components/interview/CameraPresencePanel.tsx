import { useCallback, useEffect, useRef, useState } from "react";

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
  active: boolean;
  onSignalChange?: (signal: {
    presence: string;
    eyeContact: string;
    confidence: string;
    nervousness: string;
  }) => void;
}

const OFF_SIGNALS = {
  presence: "Voice-based emotion analysis only.",
  eyeContact: "Camera activates when you start voice recording.",
  confidence: "Visual confidence scoring is inactive.",
  nervousness: "Visual nervousness scoring is inactive."
};

export function CameraPresencePanel({ enabled, active, onSignalChange }: CameraPresencePanelProps) {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const monitorHandleRef = useRef<number | null>(null);
  const previousFaceWidthRef = useRef<number | null>(null);
  const missingFramesRef = useRef(0);

  const [status, setStatus] = useState("Camera is on standby. It will activate when you start voice recording.");
  const [presenceSignal, setPresenceSignal] = useState(OFF_SIGNALS.presence);
  const [eyeContactProxy, setEyeContactProxy] = useState(OFF_SIGNALS.eyeContact);
  const [confidenceSignal, setConfidenceSignal] = useState(OFF_SIGNALS.confidence);
  const [nervousnessSignal, setNervousnessSignal] = useState(OFF_SIGNALS.nervousness);

  const publishSignals = useCallback(
    (presence: string, eyeContact: string, confidence: string, nervousness: string) => {
      onSignalChange?.({ presence, eyeContact, confidence, nervousness });
    },
    [onSignalChange]
  );

  const stopCamera = useCallback(() => {
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
    previousFaceWidthRef.current = null;
    missingFramesRef.current = 0;
  }, []);

  useEffect(() => {
    if (!enabled || !active) {
      stopCamera();
      const standbyMsg = !enabled
        ? "Camera mode is off for this session."
        : "Camera is on standby. It will activate when you start voice recording.";
      setStatus(standbyMsg);
      setPresenceSignal(OFF_SIGNALS.presence);
      setEyeContactProxy(OFF_SIGNALS.eyeContact);
      setConfidenceSignal(OFF_SIGNALS.confidence);
      setNervousnessSignal(OFF_SIGNALS.nervousness);
      publishSignals(OFF_SIGNALS.presence, OFF_SIGNALS.eyeContact, OFF_SIGNALS.confidence, OFF_SIGNALS.nervousness);
      return;
    }

    let cancelled = false;

    const startCamera = async () => {
      if (!navigator.mediaDevices?.getUserMedia) {
        setStatus("Camera capture is not supported in this browser.");
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

        setStatus("Camera feed is active for this answer.");

        if (!window.FaceDetector) {
          const p = "Camera feed is active.";
          const e = "This browser does not expose face detection, so visual analysis stays in readiness mode.";
          const c = "Camera is on, but browser-side confidence scoring is limited.";
          const n = "Use the voice scorecard for nervousness signals in this browser.";
          setPresenceSignal(p);
          setEyeContactProxy(e);
          setConfidenceSignal(c);
          setNervousnessSignal(n);
          publishSignals(p, e, c, n);
          return;
        }

        const detector = new window.FaceDetector({ fastMode: true, maxDetectedFaces: 1 });
        monitorHandleRef.current = window.setInterval(async () => {
          if (!videoRef.current || videoRef.current.readyState < 2) return;

          try {
            const faces = await detector.detect(videoRef.current);
            if (!faces.length) {
              missingFramesRef.current += 1;
              const p = "No face detected in frame.";
              const e = "Move back into frame to strengthen visual confidence signals.";
              const c = "Confidence read is unavailable while you are out of frame.";
              const n =
                missingFramesRef.current >= 2
                  ? "Frequent frame breaks detected. This can read like nervous energy in a mock interview."
                  : "Stay in frame for more stable visual analysis.";
              setPresenceSignal(p);
              setEyeContactProxy(e);
              setConfidenceSignal(c);
              setNervousnessSignal(n);
              publishSignals(p, e, c, n);
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
            /* sampling error — keep going */
          }
        }, 2500);
      } catch {
        setStatus("Camera access was blocked or unavailable.");
      }
    };

    startCamera();

    return () => {
      cancelled = true;
      stopCamera();
    };
  }, [enabled, active, publishSignals, stopCamera]);

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
