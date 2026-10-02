# Version 1.4.1 verification

final result: passed

Scope: user-approved refinements to the prior screenshot-matched layout in both Android and browser preview. Blue primary button (#005D9C), softer dark surfaces, date-dependent code label, last successful import plus latest available date, and long-press copy.

Browser evidence: ../../outputs/Tagescode-V1.4.1-Vorschau.png, viewport 885 x 884, logical phone 384 x 832. Checked both themes. Content viewport and scrollHeight both 788px in the today state. Footer and all four rows fit. Original logos remain sharp and use the correct theme variant. Typography and spacing retained, with modest spacing reductions to accommodate the label and status. Brand-blue button uses white text.

Interactions verified: choose October 3, label changes to selected code; Today restores today's label/date; theme switch works; copy action resolves with success confirmation. Browser clipboard readback through the automation bridge returned empty, so actual OS clipboard content remains unverified. Native Android long-press uses ClipboardManager; physical-device interaction has not been tested. No browser console errors.

Data status: Android timestamp records successful nonempty parses after cache save, including successful reads with no new dates. Failures and empty files do not update it. Preview uses its own snapshot/import time, not the phone's timestamp. Last available date is computed from the imported code set; no claim of uninterrupted coverage. VersionName is fixed at 1.4.1, while versionCode continues increasing with CI runs.

Validation: local Java compilation against Android API succeeds; all 9 existing parser/date tests pass. GitHub build run 37018468658 is checked separately before release handoff.

No actionable P0/P1/P2 visual findings. Browser and Samsung typography can vary slightly. Android system chrome is not emulated.

