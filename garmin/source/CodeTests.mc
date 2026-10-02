import Toybox.Test;
import Toybox.Application;

(:test)
function codePacketTest(logger) {
    var saved = Application.Storage.getValue("codes");
    var incoming = {};
    incoming[Codes.todayKey()] = "012345";
    Test.assert(Codes.receive({"version" => 1, "codes" => incoming}));
    Test.assertEqual(Codes.current(), "012345");
    // Reject malformed updates without losing the valid cache.
    Test.assert(!Codes.receive({"version" => 2, "codes" => {}}));
    Test.assert(!Codes.receive({"version" => 1, "codes" => {"2026-10-03" => "ABCDEF"}}));
    Test.assertEqual(Codes.current(), "012345");
    // Historical codes must not stand in for today's missing entry.
    Test.assert(Codes.receive({"version" => 1, "codes" => {"2000-01-01" => "654321"}}));
    Test.assertEqual(Codes.current(), "Kein Code");
    Test.assert(Codes.receive({"version" => 1, "codes" => {}}));
    Test.assertEqual(Codes.current(), "Kein Code");
    Application.Storage.setValue("codes", saved);
    return true;
}
