//This component wraps a channel of noteOn and noteOff
//into on single components, instead of having each of the
//notes in separate components.
//This is handy when midi note messages are received from
//keyboards and/or sequencers, thus reducing the amount of components.

MIDIDeviceNoteComponent : MIDIDeviceComponent {
	var noteStates;
	var noteOnResponder, noteOffResponder;
	var >noteOnAction;
	var >noteOffAction;

	init{
		|
		midiIn_, midiOut_, chan_, number_, msgType_, 
		argTemplate_, name_, controllerName_, spec_, syncFunc_
		|

		noteStates = (velocity: 0, channel: 0) ! 127;
		super.init(
			midiIn_, midiOut_, chan_, number_, msgType_,
			argTemplate_, name_, controllerName_, spec_, syncFunc_
		);
	}

	prSetupResponderAndSyncFunc{
		noteOnResponder = MIDIFunc({arg velocity, num, chan, src;
			this.valueAction_(num, velocity, chan);
		}, number, chan, \noteOn, midiIn.uid, argTemplate: argTemplate);
		noteOffResponder = MIDIFunc({arg velocity, num, chan, src;
			this.valueAction_(num, 0, chan);
		}, number, chan, \noteOff, midiIn.uid, argTemplate: argTemplate);
		noteOnResponder.permanent_(true);
		noteOffResponder.permanent_(true);
	}

	setNoteOn{|notenum, velocity, channel|
		noteStates[notenum].put(\velocity, velocity);
		noteStates[notenum].put(\channel, channel);
		this.changed(\noteOn, notenum, velocity, channel);
	}

	setNoteOff{|notenum, channel|
		noteStates[notenum].put(\velocity, 0);
		noteStates[notenum].put(\channel, channel);
		this.changed(\noteOff, notenum, channel);
	}

	killAllNotes{
		noteStates.do({|v, i|
			this.setNoteOff(v);
		});
	}

	value_{|notenum, velocity, channel|
		if(velocity == 0, {
			this.setNoteOff(notenum, channel);
		}, {
			this.setNoteOn(notenum, velocity, channel);
		});
		this.changed(\value);
	}

	valueAction_{arg notenum, velocity, channel;
		this.value_(notenum, velocity, channel);
		this.doAction(notenum, velocity, channel);
	}

	doAction{|notenum, velocity, channel|
		if(velocity == 0, {
			noteOffAction.value(this, notenum, channel);
		}, {
			noteOnAction.value(this, notenum, velocity, channel);
		});
		action.value(this, notenum , velocity, channel);
	}

	free{
		if(traceResponder.notNil, {
			traceResponder.remove;
			traceResponder = nil;
		});
		noteOnResponder.remove;
		noteOnResponder = nil;
		noteOffResponder.remove;
		noteOffResponder = nil;
	}

	trace{arg bool = true;
		if(traceResponder.notNil, {
			traceResponder.remove;
			traceResponder = nil;
		});
		if(bool, {
			traceResponder = SimpleController.new(this);
			traceResponder.put(\noteOn, {arg comp, what, notenum, velocity, channel;
				"%/% noteOn - notenum: % vel: % chan: %".format(
					controllerName, name, notenum, velocity, channel
				).postln;
			});
			traceResponder.put(\noteOff, {arg comp, what, notenum, channel;
				"%/% noteOff - notenum: % chan: %".format(
					controllerName, name, notenum, channel
				).postln;
			});
		});

	}

}
