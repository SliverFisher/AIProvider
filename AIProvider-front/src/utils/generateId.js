import { v4 as uuidv4 } from "uuid";

export function generateId() {
  const nativeRandomUUID = globalThis.crypto?.randomUUID;
  return typeof nativeRandomUUID === "function"
    ? nativeRandomUUID.call(globalThis.crypto)
    : uuidv4();
}
