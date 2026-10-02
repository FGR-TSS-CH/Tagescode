# Preview visual verification

final result: passed

Source: C:/Users/FLORIA~1.GRO/AppData/Local/Temp/codex-clipboard-2619f202-a667-4bf5-8986-039496a646f0.png
Implementation: ../../outputs/Tagescode-Handy-Vorschau.png
URL: http://127.0.0.1:8876/Tagescode-Vorschau.html
Viewport and screenshot: 885 x 884 browser pixels. Source: 709 x 1536 pixels.
Phone CSS viewport: 384 x 832, same 19.5:9 ratio. Source measurements divided by 709/384; browser stage proportionally fitted to available height. Compared app regions in both images together, excluding browser controls, outer bezel, and Android-owned system chrome.
State: dark, 2 October 2026, today's code. Also inspected light theme.

Findings and fixes:
- Previous preview had smaller code, rows and typography, short cards and excess footer spacing. Matched Android layout dimensions: 22px side inset, 225x72 logo, 168px code card, 68px code, 18px date, 56px button, 51px rows and 19px section title. Embedded Roboto with its license; reused original Android calendar vector and Swiss badge.
- First verification found 3px internal overflow. Increased app viewport to 788px with 44px reserved bottom area; final measured scrollHeight equals clientHeight (788), page scrollHeight equals viewport height (884).

Required surfaces:
- Typography: Roboto, Android font sizes and weights, correct single-line dates and rows. Samsung font rendering may differ slightly (P3).
- Spacing: card top 122px and primary button top 310px match normalized reference. Additional section and footer follow native margins. No normal-state clipping.
- Colors: intentionally use user-requested Videojet 2024 palette from current app 1.3.112 rather than superseded 1.3.106 screenshot. Yellow CTA, original white/black wordmarks by theme.
- Images: original supplied logo files retain transparency/aspect ratio; original app vector paths reused without approximation.
- Copy: same date formatting, four extra codes, attribution and current build footer.

Full-view evidence: source and saved implementation opened together. Focused comparison: code/date, row typography and footer all readable in those views; separate crops unnecessary.
Interaction checks: calendar opens; missing October dates disabled; selecting October 3 updates date; Today restores October 2; both themes show the appropriate logo. Browser console error log empty.

Expected limitations: Android status/navigation icons are not simulated; browser is an interactive layout preview, not an Android emulator. Selecting another date adds the native Today control and allows internal content scrolling, as Android does. The browser page itself remains fitted.

Checklist completed: layout matching, original assets, typography, light/dark, calendar, overflow verification. No remaining P0/P1/P2 issue within this scope.
