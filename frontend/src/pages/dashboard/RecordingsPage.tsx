import { useEffect, useState } from "react";
import { SectionCard } from "../../components/ui/SectionCard";

interface Recording {
  fileName: string;
  fileSize: number;
  createdAt: number;
  url: string;
}

export function RecordingsPage() {
  const [recordings, setRecordings] = useState<Recording[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedVideo, setSelectedVideo] = useState<Recording | null>(null);

  useEffect(() => {
    loadRecordings();
  }, []);

  const loadRecordings = async () => {
    setLoading(true);
    setError("");
    try {
      const token = localStorage.getItem("token");
      const response = await fetch("http://localhost:8080/api/interviews/recordings/list", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        setRecordings(data);
      } else {
        setError("Failed to load recordings");
      }
    } catch {
      setError("Error loading recordings");
    } finally {
      setLoading(false);
    }
  };

  const deleteRecording = async (fileName: string) => {
    if (!confirm("Are you sure you want to delete this recording?")) return;

    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`http://localhost:8080/api/interviews/recordings/${fileName}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.ok) {
        setRecordings(recordings.filter((r) => r.fileName !== fileName));
        if (selectedVideo?.fileName === fileName) {
          setSelectedVideo(null);
        }
      }
    } catch {
      alert("Failed to delete recording");
    }
  };

  const formatDate = (timestamp: number) => {
    return new Date(timestamp).toLocaleString();
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + " KB";
    return (bytes / (1024 * 1024)).toFixed(1) + " MB";
  };

  if (loading) {
    return (
      <div className="glass-panel rounded-[1.75rem] p-6 text-gray-700">
        Loading your interview recordings...
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <SectionCard
        title="Interview Recordings"
        subtitle="Review your past interview performances and track your progress over time"
      >
        {error && <p className="mb-4 text-sm text-rose-600">{error}</p>}

        {recordings.length === 0 ? (
          <div className="app-surface rounded-[1.5rem] p-8 text-center text-gray-600">
            <p className="text-lg">No recordings yet</p>
            <p className="mt-2 text-sm">
              Enable camera mode during your interview sessions to automatically record and save your performance
            </p>
          </div>
        ) : (
          <div className="grid gap-4 lg:grid-cols-2">
            {recordings.map((recording) => (
              <div
                key={recording.fileName}
                className="app-surface rounded-[1.5rem] p-5 transition hover:shadow-lg"
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <p className="font-display text-lg text-gray-900">
                      {recording.fileName.replace(/^interview_/, "").replace(/\.(webm|mp4)$/, "")}
                    </p>
                    <p className="mt-1 text-xs text-gray-600">{formatDate(recording.createdAt)}</p>
                    <p className="mt-1 text-xs text-gray-500">{formatFileSize(recording.fileSize)}</p>
                  </div>
                  <div className="flex gap-2">
                    <button
                      type="button"
                      onClick={() => setSelectedVideo(recording)}
                      className="app-button-primary rounded-full px-4 py-2 text-xs font-medium transition"
                    >
                      Watch
                    </button>
                    <button
                      type="button"
                      onClick={() => deleteRecording(recording.fileName)}
                      className="rounded-full bg-rose-600 px-4 py-2 text-xs font-medium text-white transition hover:bg-rose-700"
                    >
                      Delete
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </SectionCard>

      {/* Video Player Modal */}
      {selectedVideo && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4"
          onClick={() => setSelectedVideo(null)}
        >
          <div
            className="relative max-w-4xl w-full bg-gray-900 rounded-xl overflow-hidden shadow-2xl"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="bg-gray-800 px-4 py-3 flex items-center justify-between">
              <p className="text-white font-medium">{selectedVideo.fileName}</p>
              <button
                type="button"
                onClick={() => setSelectedVideo(null)}
                className="text-white/70 hover:text-white transition text-xl px-2"
              >
                ×
              </button>
            </div>
            <div className="aspect-video bg-black">
              <video
                controls
                autoPlay
                className="w-full h-full"
                src={`http://localhost:8080${selectedVideo.url}?token=${localStorage.getItem("token")}`}
              >
                Your browser does not support video playback.
              </video>
            </div>
            <div className="bg-gray-800 px-4 py-3 text-sm text-gray-400">
              <p>Recorded: {formatDate(selectedVideo.createdAt)}</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
