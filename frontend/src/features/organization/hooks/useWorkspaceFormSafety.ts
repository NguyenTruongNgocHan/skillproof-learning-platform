import { useEffect, useId, useRef } from "react";
import { useOrganizationContext } from "../providers/OrganizationProvider";
function signature(form: HTMLFormElement) {
  return JSON.stringify(
    Array.from(form.elements)
      .filter(
        (e): e is HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement =>
          e instanceof HTMLInputElement ||
          e instanceof HTMLSelectElement ||
          e instanceof HTMLTextAreaElement,
      )
      .filter((e) => !(e instanceof HTMLInputElement && e.type === "file"))
      .map((e) => [
        e.getAttribute("name"),
        e instanceof HTMLInputElement && ["checkbox", "radio"].includes(e.type)
          ? e.checked
          : e.value,
      ]),
  );
}
export function useWorkspaceFormSafety() {
  const root = useRef<HTMLDivElement>(null),
    id = useId();
  const { setDirty } = useOrganizationContext();
  useEffect(() => {
    const node = root.current;
    if (!node) return;
    const forms = new Map<
      HTMLFormElement,
      { key: string; baseline: string; dirty: boolean }
    >();
    let nextFormKey = 0;
    const discover = () => {
      node.querySelectorAll("form").forEach((form) => {
        if (!forms.has(form))
          forms.set(form, {
            key: `form-${id}-${nextFormKey++}`,
            baseline: signature(form),
            dirty: false,
          });
      });
      for (const [form, state] of forms)
        if (!node.contains(form)) {
          setDirty(state.key, false);
          forms.delete(form);
        }
    };
    discover();
    const observer = new MutationObserver(discover);
    observer.observe(node, { childList: true, subtree: true });
    const change = (event: Event) => {
      discover();
      const form = (event.target as Element)?.closest("form");
      if (!form) return;
      const state = forms.get(form);
      if (state) {
        state.dirty = signature(form) !== state.baseline;
        setDirty(state.key, state.dirty);
      }
    };
    const saved = (event: Event) => {
      const form = (event.target as Element)?.closest("form");
      const state = form && forms.get(form);
      if (state) {
        state.baseline = signature(form!);
        state.dirty = false;
        setDirty(state.key, false);
      }
    };
    const tab = (event: Event) => {
      const button = (event.target as Element)?.closest('[role="tab"]');
      if (
        node.querySelector("[data-course-studio]") ||
        !button ||
        button.getAttribute("aria-selected") === "true" ||
        ![...forms.values()].some((s) => s.dirty)
      )
        return;
      if (
        !window.confirm(
          "Discard unsaved form changes before switching this section?",
        )
      ) {
        event.preventDefault();
        event.stopPropagation();
      } else
        for (const state of forms.values()) {
          state.dirty = false;
          setDirty(state.key, false);
        }
    };
    node.addEventListener("input", change);
    node.addEventListener("change", change);
    node.addEventListener("workspace:saved", saved);
    node.addEventListener("click", tab, true);
    return () => {
      observer.disconnect();
      node.removeEventListener("input", change);
      node.removeEventListener("change", change);
      node.removeEventListener("workspace:saved", saved);
      node.removeEventListener("click", tab, true);
      for (const state of forms.values()) setDirty(state.key, false);
    };
  }, [id, setDirty]);
  return root;
}
