# Version 1.4.2 layout verification

final result: passed

User requirement: use Zuletzt geprüft and keep the complete screen visible when selecting another date.
Implementation evidence: ../../outputs/Tagescode-V1.4.2-Vorschau.png; browser 885 x 884, phone CSS 384 x 832.

Today and a selected October 3 date use the same 56px horizontal action row. Today is beside the date picker, not a new row. Measured content height and scrollHeight are both 788px in selected-date state. Footer stays visible. Code label, four additional codes, metadata, typography, colors, original logos and footer verified visually. Copy/layout unchanged otherwise.

Android replaces the scrolling parent with FitScreenLayout: measure full content, then scale down only if needed to fit the available height, with horizontal centering. No content hidden to remove scrolling. Reduced bottom padding to 12dp. Selected-date button visibility follows actual date rather than selection method.

Local Android API compilation and 9 parser/calendar tests pass. XML parses. Physical-phone rendering not tested; final APK compiled by GitHub Actions before handoff.

No remaining P0/P1/P2 findings. System font/rendering differences remain possible; unusually large accessibility text will be proportionally fitted.
