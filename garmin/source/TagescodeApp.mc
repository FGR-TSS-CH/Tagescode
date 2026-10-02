import Toybox.Application;
import Toybox.Background;
import Toybox.Communications;
import Toybox.Complications;
import Toybox.Graphics;
import Toybox.Lang;
import Toybox.System;
import Toybox.Time;
import Toybox.Time.Gregorian;
import Toybox.Timer;
import Toybox.WatchUi;

(:background, :glance)
module Codes {
    function todayKey() {
        var date = Gregorian.info(Time.now(), Time.FORMAT_SHORT);
        return date.year.format("%04d") + "-" + date.month.format("%02d") + "-" + date.day.format("%02d");
    }

    function current() {
        var codes = Application.Storage.getValue("codes");
        if (codes instanceof Dictionary) {
            var code = codes[todayKey()];
            if (validCode(code)) { return code; }
        }
        return "Kein Code";
    }

    function validCode(code) {
        if (!(code instanceof String) || code.length() != 6) { return false; }
        for (var i = 0; i < 6; i++) {
            var digit = code.substring(i, i + 1);
            if ("0123456789".find(digit) == null) { return false; }
        }
        return true;
    }

    function receive(data) {
        if (!(data instanceof Dictionary) || data["version"] != 1) { return false; }
        var incoming = data["codes"];
        if (!(incoming instanceof Dictionary) || incoming.size() > 33) { return false; }
        var keys = incoming.keys();
        for (var i = 0; i < keys.size(); i++) {
            var key = keys[i];
            if (!(key instanceof String) || key.length() != 10 || !validCode(incoming[key])) {
                return false;
            }
        }
        // Replace atomically, including an empty packet. Never retain dates absent in a new snapshot.
        Application.Storage.setValue("codes", incoming);
        publish();
        return true;
    }

    function publish() {
        // String values preserve all six digits, including leading zeroes.
        Complications.updateComplication(0, { :value => current(), :unit => Complications.UNIT_INVALID });
    }
}

(:background, :glance)
class TagescodeApp extends Application.AppBase {
    function initialize() { AppBase.initialize(); }

    function onStart(state) {
        Communications.registerForPhoneAppMessages(method(:onPhone));
        Background.registerForPhoneAppMessageEvent();
        Codes.publish();
        // Garmin schedules execution; a date change in the watch face may be delayed.
        if (Background.getTemporalEventRegisteredTime() == null) {
            Background.registerForTemporalEvent(new Time.Duration(300));
        }
    }

    function onPhone(message as Communications.PhoneAppMessage) as Void {
        if (Codes.receive(message.data)) { WatchUi.requestUpdate(); }
    }

    function getInitialView() { return [new CodeView()]; }
    function getGlanceView() { return [new CodeGlance()]; }
    function getServiceDelegate() { return [new CodeService()]; }
    function onStorageChanged() { Codes.publish(); WatchUi.requestUpdate(); }
}

(:background)
class CodeService extends System.ServiceDelegate {
    function initialize() { ServiceDelegate.initialize(); }
    function onPhoneAppMessage(message as Communications.PhoneAppMessage) as Void {
        Codes.receive(message.data);
        Background.exit(null);
    }
    function onTemporalEvent() {
        Codes.publish();
        Background.exit(null);
    }
}

class CodeView extends WatchUi.View {
    var ticker;
    var dismissTimer;
    function initialize() { View.initialize(); }
    function onShow() {
        ticker = new Timer.Timer();
        ticker.start(method(:refresh), 1000, true);
        dismissTimer = new Timer.Timer();
        dismissTimer.start(method(:dismiss), 10000, false);
    }
    function refresh() as Void { WatchUi.requestUpdate(); }
    function dismiss() as Void { System.exit(); }
    function onHide() {
        if (ticker != null) { ticker.stop(); ticker = null; }
        if (dismissTimer != null) { dismissTimer.stop(); dismissTimer = null; }
    }
    function onUpdate(dc) {
        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_BLACK);
        dc.clear();
        var value = Codes.current();
        var font = Graphics.FONT_NUMBER_HOT;
        if (value.equals("Kein Code") || dc.getTextWidthInPixels(value, font) > dc.getWidth() - 48) {
            font = Graphics.FONT_LARGE;
        }
        dc.drawText(dc.getWidth()/2, dc.getHeight()/2, font, value,
            Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
    }
}

(:glance)
class CodeGlance extends WatchUi.GlanceView {
    function initialize() { GlanceView.initialize(); }
    function onUpdate(dc) {
        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_BLACK);
        dc.clear();
        dc.drawText(dc.getWidth()/2, dc.getHeight()/2, Graphics.FONT_LARGE, Codes.current(),
            Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
    }
}
