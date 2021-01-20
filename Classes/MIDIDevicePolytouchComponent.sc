MIDIDevicePolytouchComponent : MIDIDeviceComponent {
	var touchValues;

	init{
		|
		midiIn_, midiOut_, chan_, number_, msgType_, 
		argTemplate_, name_, controllerName_, spec_, syncFunc_
		|

		touchValues = 0 ! 127;
		super.init(
			midiIn_, midiOut_, chan_, number_, msgType_,
			argTemplate_, name_, controllerName_, spec_, syncFunc_
		);
	}

	prSetupResponderAndSyncFunc{
		responder = MIDIFunc({arg val, num, chan, src;
			this.valueAction_(num, val);
		}, nil, chan, \polytouch, midiIn.uid, argTemplate: argTemplate);
		responder.permanent_(true);
	}

	value_{arg notenum, val;
		touchValues[notenum] = val;
		this.changed(\value, notenum, val);
	}

	valueAction_{arg notenum, val;
		this.value_(notenum, val);
		this.doAction(notenum, val);
	}

	doAction{|notenum, val|
		this.action.value(this, notenum, val);
	}

	trace{arg bool = true;
		if(traceResponder.notNil, {
			traceResponder.remove;
			traceResponder = nil;
		});
		if(bool, {
			traceResponder = SimpleController.new(this);
			traceResponder.put(\value, {arg comp, what, notenum, val;
				"%/% - polytouch notenum: % val: %".format(
					controllerName, name, notenum, val
				).postln;
			});
		});
	}
}
