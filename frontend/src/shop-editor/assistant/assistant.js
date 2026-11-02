import { RULES } from "./rules/index.js";

// Design assistant (shop-design-assistant spec): runs every rule on the current drafts and
// returns findings in display order. Deterministic and local; nothing is sent anywhere.
//
// ctx: { page: ShopPageDto, blocks: draft blocks, theme: theme draft,
//        images: { [assetId]: { sizeBytes, width } } (filled in as known) }
// finding: { id, ruleId, severity, scope, blockId, field, role, target, emphasis, title, why, fix }

export const SEVERITIES = {
  blocker: { label: "Blocker", icon: "octagon-x", order: 0 },
  warning: { label: "Warnung", icon: "triangle-alert", order: 1 },
  tip: { label: "Tipp", icon: "lightbulb", order: 2 },
};

export function evaluate(ctx, rules = RULES) {
  const findings = [];
  rules.forEach((rule, ruleIndex) => {
    for (const detail of rule.check(ctx)) {
      const severity = detail.severity ?? rule.severity;
      findings.push({
        id: [rule.id, detail.blockId ?? "", detail.key ?? ""].join(":"),
        ruleId: rule.id,
        severity,
        scope: rule.scope,
        blockId: detail.blockId ?? null,
        field: detail.field ?? null,
        role: detail.role ?? null,
        target: detail.target ?? (rule.scope === "theme" ? "theme" : detail.blockId ? "block" : null),
        emphasis: Boolean(rule.emphasis),
        ...rule.explain({ ...detail, severity }),
        order: [SEVERITIES[severity].order, rule.emphasis ? 0 : 1, ruleIndex],
      });
    }
  });
  const block = (finding) => ctx.blocks.findIndex((candidate) => candidate.id === finding.blockId);
  return findings.sort((a, b) => a.order[0] - b.order[0] || a.order[1] - b.order[1] || a.order[2] - b.order[2]
    || block(a) - block(b));
}

// What publishing a page or the design depends on (spec "Publish gating"): Tipps never count.
export function gate(findings, scopes) {
  const relevant = findings.filter((finding) => scopes.includes(finding.scope));
  return {
    blockers: relevant.filter((finding) => finding.severity === "blocker"),
    warnings: relevant.filter((finding) => finding.severity === "warning"),
  };
}

export function countBySeverity(findings) {
  const counts = { blocker: 0, warning: 0, tip: 0 };
  findings.forEach((finding) => {
    counts[finding.severity] += 1;
  });
  return counts;
}
