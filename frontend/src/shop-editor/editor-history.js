// Undo/redo for the current editing session (design.md Decision 2): a bounded stack of draft
// snapshots, nothing on the server. Typing in one field within COALESCE_MS counts as one step.
const LIMIT = 50;
const COALESCE_MS = 1000;

export class EditorHistory {
  #undo = [];
  #redo = [];
  #lastKey = null;
  #lastAt = 0;

  // Call before applying a change, with the state as it was. key groups rapid edits of
  // the same field (e.g. "<blockId>:title"); structural changes pass no key.
  record(snapshot, key = null) {
    const now = Date.now();
    const coalesce = key !== null && key === this.#lastKey && now - this.#lastAt < COALESCE_MS;
    this.#lastKey = key;
    this.#lastAt = now;
    if (coalesce) return;
    this.#undo.push(snapshot);
    if (this.#undo.length > LIMIT) this.#undo.shift();
    this.#redo = [];
  }

  // Returns the snapshot to restore, or null.
  undo(current) {
    if (!this.#undo.length) return null;
    this.#redo.push(current);
    this.#lastKey = null;
    return this.#undo.pop();
  }

  redo(current) {
    if (!this.#redo.length) return null;
    this.#undo.push(current);
    this.#lastKey = null;
    return this.#redo.pop();
  }

  // Snapshot the next undo/redo would restore, without taking it.
  get nextUndo() {
    return this.#undo.at(-1) ?? null;
  }

  get nextRedo() {
    return this.#redo.at(-1) ?? null;
  }

  get canUndo() {
    return this.#undo.length > 0;
  }

  get canRedo() {
    return this.#redo.length > 0;
  }

  clear() {
    this.#undo = [];
    this.#redo = [];
    this.#lastKey = null;
  }
}
