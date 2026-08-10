const storageKey = "min-note-web-notes";

const noteSearch = document.querySelector("#noteSearch");
const noteList = document.querySelector("#noteList");
const newNoteButton = document.querySelector("#newNoteButton");
const titleInput = document.querySelector("#titleInput");
const bodyInput = document.querySelector("#bodyInput");
const findInput = document.querySelector("#findInput");
const replaceInput = document.querySelector("#replaceInput");
const replaceButton = document.querySelector("#replaceButton");
const saveButton = document.querySelector("#saveButton");
const deleteButton = document.querySelector("#deleteButton");
const matchCount = document.querySelector("#matchCount");
const status = document.querySelector("#status");
const lineNumbers = document.querySelector("#lineNumbers");
const highlightLayer = document.querySelector("#highlightLayer");
const scrollSlider = document.querySelector("#scrollSlider");

let notes = loadNotes();
let activeId = notes[0]?.id || createNote("Untitled note", "");
let syncingSlider = false;

renderList();
selectNote(activeId);

newNoteButton.addEventListener("click", () => {
  activeId = createNote("Untitled note", "");
  renderList();
  selectNote(activeId);
  titleInput.focus();
  titleInput.select();
});

saveButton.addEventListener("click", saveActiveNote);
deleteButton.addEventListener("click", deleteActiveNote);

noteSearch.addEventListener("input", renderList);

titleInput.addEventListener("input", () => {
  saveActiveNote(false);
  renderList();
});

bodyInput.addEventListener("input", () => {
  saveActiveNote(false);
  updateEditorMeta();
});

bodyInput.addEventListener("scroll", () => {
  lineNumbers.scrollTop = bodyInput.scrollTop;
  highlightLayer.scrollTop = bodyInput.scrollTop;
  highlightLayer.scrollLeft = bodyInput.scrollLeft;
  syncSliderFromEditor();
});

findInput.addEventListener("input", updateHighlights);

replaceButton.addEventListener("click", () => {
  const query = findInput.value;
  if (!query) {
    setStatus("Enter a word to replace");
    return;
  }

  const matches = findMatches(bodyInput.value, query);
  if (!matches.length) {
    setStatus("No matches");
    return;
  }

  const replacement = replaceInput.value;
  bodyInput.value = replaceAllCaseInsensitive(bodyInput.value, query, replacement);
  saveActiveNote(false);
  updateEditorMeta();
  setStatus(`${matches.length} replaced`);
});

scrollSlider.addEventListener("input", () => {
  if (syncingSlider) {
    return;
  }
  const maxScroll = Math.max(0, bodyInput.scrollHeight - bodyInput.clientHeight);
  bodyInput.scrollTop = Math.round(maxScroll * (Number(scrollSlider.value) / 100));
});

window.addEventListener("resize", updateEditorMeta);

function loadNotes() {
  try {
    const parsed = JSON.parse(localStorage.getItem(storageKey) || "[]");
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function persistNotes() {
  localStorage.setItem(storageKey, JSON.stringify(notes));
}

function createNote(title, body) {
  const id = crypto.randomUUID();
  notes.unshift({
    id,
    title,
    body,
    updatedAt: Date.now()
  });
  persistNotes();
  return id;
}

function activeNote() {
  return notes.find((note) => note.id === activeId);
}

function selectNote(id) {
  activeId = id;
  const note = activeNote();
  if (!note) {
    return;
  }
  titleInput.value = note.title;
  bodyInput.value = note.body;
  renderList();
  updateEditorMeta();
  setStatus("Ready");
}

function saveActiveNote(showStatus = true) {
  const note = activeNote();
  if (!note) {
    return;
  }
  note.title = titleInput.value.trim() || "Untitled note";
  note.body = bodyInput.value;
  note.updatedAt = Date.now();
  notes = [note, ...notes.filter((item) => item.id !== note.id)];
  persistNotes();
  if (showStatus) {
    setStatus("Saved");
  }
}

function deleteActiveNote() {
  if (!activeNote()) {
    return;
  }
  if (!confirm("Delete this note?")) {
    return;
  }
  notes = notes.filter((note) => note.id !== activeId);
  if (!notes.length) {
    activeId = createNote("Untitled note", "");
  } else {
    activeId = notes[0].id;
    persistNotes();
  }
  renderList();
  selectNote(activeId);
  setStatus("Deleted");
}

function renderList() {
  const query = noteSearch.value.trim().toLowerCase();
  const visible = notes.filter((note) => {
    const haystack = `${note.title}\n${note.body}`.toLowerCase();
    return haystack.includes(query);
  });

  noteList.innerHTML = "";
  if (!visible.length) {
    const empty = document.createElement("div");
    empty.className = "empty";
    empty.textContent = "No notes found.";
    noteList.append(empty);
    return;
  }

  for (const note of visible) {
    const item = document.createElement("button");
    item.type = "button";
    item.className = `note-item${note.id === activeId ? " active" : ""}`;
    item.addEventListener("click", () => selectNote(note.id));

    const title = document.createElement("div");
    title.className = "note-title";
    title.textContent = note.title || "Untitled note";

    const preview = document.createElement("div");
    preview.className = "note-preview";
    preview.textContent = note.body.replace(/\s+/g, " ").trim() || "Empty note";

    item.append(title, preview);
    noteList.append(item);
  }
}

function updateEditorMeta() {
  updateLineNumbers();
  updateHighlights();
  syncSliderFromEditor();
}

function updateLineNumbers() {
  const count = Math.max(1, bodyInput.value.split("\n").length);
  lineNumbers.textContent = Array.from({ length: count }, (_, index) => index + 1).join("\n");
}

function updateHighlights() {
  const query = findInput.value;
  const body = bodyInput.value;
  if (!query) {
    highlightLayer.textContent = body;
    matchCount.textContent = "";
    return;
  }

  const matches = findMatches(body, query);
  let html = "";
  let offset = 0;
  for (const match of matches) {
    html += escapeHtml(body.slice(offset, match.index));
    html += `<mark>${escapeHtml(body.slice(match.index, match.index + query.length))}</mark>`;
    offset = match.index + query.length;
  }
  html += escapeHtml(body.slice(offset));
  highlightLayer.innerHTML = html || "\n";
  matchCount.textContent = matches.length ? `${matches.length} found` : "No matches";
  if (query) {
    setStatus(matches.length ? `${matches.length} found` : "No matches");
  }
}

function findMatches(text, query) {
  if (!query) {
    return [];
  }
  const matches = [];
  const haystack = text.toLowerCase();
  const needle = query.toLowerCase();
  let index = haystack.indexOf(needle);
  while (index >= 0) {
    matches.push({ index });
    index = haystack.indexOf(needle, index + needle.length);
  }
  return matches;
}

function replaceAllCaseInsensitive(text, query, replacement) {
  const escaped = query.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  return text.replace(new RegExp(escaped, "gi"), replacement);
}

function syncSliderFromEditor() {
  const maxScroll = Math.max(0, bodyInput.scrollHeight - bodyInput.clientHeight);
  syncingSlider = true;
  scrollSlider.disabled = maxScroll === 0;
  scrollSlider.value = maxScroll === 0 ? 0 : Math.round((bodyInput.scrollTop / maxScroll) * 100);
  syncingSlider = false;
}

function setStatus(message) {
  status.textContent = message;
}

function escapeHtml(value) {
  return value.replace(/[&<>"']/g, (char) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;"
  }[char]));
}
