import { useState } from "react";
import { feedbackApi } from "../api/feedbackApi";
import { useAction } from "@/shared/hooks/useRemote";
import { ActionNotice } from "@/shared/ui/RemoteState";
import Button from "@/shared/ui/Button";
export function FeedbackForm({
  type,
  target,
}: {
  type:
    "COURSE" | "RESOURCE" | "CERTIFICATE" | "ASSESSMENT" | "QUESTION_VERSION";
  target: string;
}) {
  const [rating, setRating] = useState(5),
    [body, setBody] = useState("");
  const action = useAction();
  return (
    <form
      className="sporg-form"
      onSubmit={(e) => {
        e.preventDefault();
        void action.run(
          () => feedbackApi.write(type, target, rating, body),
          "Feedback saved.",
        );
      }}
    >
      <h2>Share your experience</h2>
      <p>
        Reviews require verified learning experience. Assessment feedback is
        private; certificate ratings describe your experience and do not
        establish credential validity.
      </p>
      <label>
        Rating
        <select
          value={rating}
          onChange={(e) => setRating(Number(e.target.value))}
        >
          {[5, 4, 3, 2, 1].map((n) => (
            <option key={n} value={n}>
              {n} / 5
            </option>
          ))}
        </select>
      </label>
      <label>
        Your feedback
        <textarea
          required
          maxLength={4000}
          value={body}
          onChange={(e) => setBody(e.target.value)}
        />
      </label>
      <ActionNotice {...action} />
      <Button type="submit" disabled={action.busy}>
        Save review
      </Button>
    </form>
  );
}
