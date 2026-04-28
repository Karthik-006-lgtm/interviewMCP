const STORAGE_KEY = "interview-prep-selected-roles";

export function loadSelectedRoles() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return [];
    }
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed.filter((item): item is string => typeof item === "string") : [];
  } catch {
    return [];
  }
}

export function saveSelectedRoles(roles: string[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(roles));
}

