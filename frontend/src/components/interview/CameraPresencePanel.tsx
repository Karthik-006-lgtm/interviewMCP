import { useEffect, useRef, useState } from "react";

interface Position {
  x: number;
  y: number;
}

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
  
  // Video recording
  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const recordedChunksRef = useRef<Blob[]>([]);
  const [isRecording, setIsRecording] = useState(false);
  const [recordingDuration, setRecordingDuration] = useState(0);
  const recordingTimerRef = useRef<number | null>(null);
  
  const [status, setStatus] = useState("Camera mode is off for this session.");
  const [presenceSignal, setPresenceSignal] = useState("Voice-based emotion analysis only.");
  const [eyeContactProxy, setEyeContactProxy] = useState("Enable camera mode to add visual presence checks.");
  const [confidenceSignal, setConfidenceSignal] = useState("Visual confidence scoring is inactive.");
  const [nervousnessSignal, setNervousnessSignal] = useState("Visual nervousness scoring is inactive.");
  
  // Draggable state
  const [position, setPosition] = useState<Position>({ x: 20, y: 20 });
  const [isDragging, setIsDragging] = useState(false);
  const [dragOffset, setDragOffset] = useState<Position>({ x: 0, y: 0 });
  const [isMinimized, setIsMinimized] = useState(false);
  const containerRef = useRef<HTMLDivElement | null>(null);

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

  // Start recording video
  const startRecording = () => {
    if (!streamRef.current) return;

    try {
      const options = {
        mimeType: 'video/webm;codecs=vp9',
      };
      
      // Fallback for Safari/browsers that don't support vp9
      if (!MediaRecorder.isTypeSupported(options.mimeType)) {
        options.mimeType = 'video/webm';
      }

      const mediaRecorder = new MediaRecorder(streamRef.current, options);
      recordedChunksRef.current = [];

      mediaRecorder.ondataavailable = (event) => {
        if (event.data && event.data.size > 0) {
          recordedChunksRef.current.push(event.data);
        }
      };

      mediaRecorder.onstop = async () => {
        const blob = new Blob(recordedChunksRef.current, { type: 'video/webm' });
        await saveRecording(blob);
      };

      mediaRecorder.start(1000); // Collect data every second
      mediaRecorderRef.current = mediaRecorder;
      setIsRecording(true);

      // Start duration timer
      recordingTimerRef.current = window.setInterval(() => {
        setRecordingDuration((prev) => prev + 1);
      }, 1000);

      console.log('Video recording started');
    } catch (error) {
      console.error('Failed to start recording:', error);
    }
  };

  // Stop recording video
  const stopRecording = () => {
    if (mediaRecorderRef.current && mediaRecorderRef.current.state !== 'inactive') {
      mediaRecorderRef.current.stop();
      mediaRecorderRef.current = null;
      setIsRecording(false);

      if (recordingTimerRef.current) {
        window.clearInterval(recordingTimerRef.current);
        recordingTimerRef.current = null;
      }

      console.log('Video recording stopped');
    }
  };

  // Save recording to backend
  const saveRecording = async (blob: Blob) => {
    try {
      const formData = new FormData();
      const fileName = `interview-recording-${Date.now()}.webm`;
      formData.append('video', blob, fileName);

      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/interviews/recordings/upload', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
        body: formData,
      });

      if (response.ok) {
        console.log('Recording saved successfully');
        setRecordingDuration(0);
        recordedChunksRef.current = [];
      } else {
        console.error('Failed to save recording');
      }
    } catch (error) {
      console.error('Error saving recording:', error);
    }
  };

  // Auto-start recording when camera starts
  useEffect(() => {
    if (enabled && streamRef.current && !isRecording) {
      // Small delay to ensure stream is ready
      const timer = setTimeout(() => {
        startRecording();
      }, 1000);
      return () => clearTimeout(timer);
    }
  }, [enabled, streamRef.current]);

  // Auto-stop recording when component unmounts or disabled
  useEffect(() => {
    return () => {
      if (isRecording) {
        stopRecording();
      }
    };
  }, [isRecording]);

  // Dragging functionality
  const handleMouseDown = (e: React.MouseEvent) => {
    if (!containerRef.current) return;
    const rect = containerRef.current.getBoundingClientRect();
    setIsDragging(true);
    setDragOffset({
      x: e.clientX - rect.left,
      y: e.clientY - rect.top,
    });
  };

  const handleMouseMove = (e: MouseEvent) => {
    if (!isDragging) return;
    
    const newX = e.clientX - dragOffset.x;
    const newY = e.clientY - dragOffset.y;
    
    // Constrain to viewport
    const maxX = window.innerWidth - (containerRef.current?.offsetWidth || 400);
    const maxY = window.innerHeight - (containerRef.current?.offsetHeight || 300);
    
    setPosition({
      x: Math.max(0, Math.min(newX, maxX)),
      y: Math.max(0, Math.min(newY, maxY)),
    });
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  useEffect(() => {
    if (isDragging) {
      window.addEventListener('mousemove', handleMouseMove);
      window.addEventListener('mouseup', handleMouseUp);
      return () => {
        window.removeEventListener('mousemove', handleMouseMove);
        window.removeEventListener('mouseup', handleMouseUp);
      };
    }
  }, [isDragging, dragOffset]);

  // Format duration as MM:SS
  const formatDuration = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <div
      ref={containerRef}
      className="fixed z-50 shadow-2xl"
      style={{
        left: `${position.x}px`,
        top: `${position.y}px`,
        width: isMinimized ? '200px' : '320px',
        cursor: isDragging ? 'grabbing' : 'grab',
      }}
    >
      <div className="bg-gray-900/95 backdrop-blur-sm rounded-xl overflow-hidden border border-gray-700/50 shadow-xl">
        {/* Draggable Header */}
        <div
          className="bg-gray-800/90 px-3 py-2 flex items-center justify-between cursor-grab active:cursor-grabbing"
          onMouseDown={handleMouseDown}
        >
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full bg-red-500 animate-pulse"></div>
            <p className="font-medium text-white text-xs">Camera</p>
          </div>
          <button
            type="button"
            onClick={() => setIsMinimized(!isMinimized)}
            className="text-white/70 hover:text-white transition text-xs px-1.5"
            title={isMinimized ? "Expand" : "Minimize"}
          >
            {isMinimized ? "□" : "−"}
          </button>
        </div>

        {/* Video Feed */}
        <div className="relative">
          <video
            ref={videoRef}
            className="w-full object-cover"
            style={{ height: isMinimized ? '150px' : '240px' }}
            autoPlay
            muted
            playsInline
          />
          {/* Recording status overlay */}
          <div className="absolute top-2 right-2 bg-black/70 px-2 py-1 rounded text-[10px] text-white font-mono flex items-center gap-1.5">
            {isRecording && (
              <>
                <div className="w-1.5 h-1.5 rounded-full bg-red-500 animate-pulse"></div>
                <span>REC {formatDuration(recordingDuration)}</span>
              </>
            )}
            {!isRecording && <span className="text-white/50">READY</span>}
          </div>
        </div>
      </div>
    </div>
  );
}
