MIDIDeviceComponent {
	var <value;
	var <responder;
	var <>syncFunction;
	var <>syncCacheValue;
	var <chan;
	var <number;
	var <spec;
	var <msgType;
	var <midiIn, <midiOut;
	var <>action;
	var traceResponder;
	var <controllerName;
	var <name;
	var <argTemplate;

	*create{arg midiIn, midiOut, chan, number, msgType, argTemplate, name, controllerName, spec, syncFunction;
		var newObj, componentClass;
		componentClass = switch(msgType, 
			\control14, MIDIDeviceComponent14BitCC,
			\midinotes, MIDIDeviceNoteComponent,
			\polytouch, MIDIDevicePolytouchComponent,
			\increment, MIDIDeviceIncrementComponent,
			this
		);
		newObj = componentClass.new(
			midiIn, midiOut, chan, number, msgType, argTemplate, name, controllerName, spec, syncFunction
		);
		^newObj;
	}

	*new{arg midiIn, midiOut, chan, number, msgType = \control, argTemplate, name, controllerName, spec, syncFunction;
		^super.new.init(midiIn, midiOut, chan, number, msgType, argTemplate, name, controllerName, spec, syncFunction);
	}

	init{arg midiIn_, midiOut_, chan_, number_, msgType_, argTemplate_, name_, controllerName_, spec_, syncFunction_;
		midiIn = midiIn_;
		midiOut = midiOut_;
		chan = chan_;
		number = number_;
		msgType = msgType_;
		name = name_;
		controllerName = controllerName_;
		value = 0;
		syncFunction = syncFunction_;
		argTemplate = argTemplate_;
		if(spec_.isNil, {
			spec = this.class.prDefaultSpec;
		}, {
			spec = spec_;
		});
		this.prSetupResponderAndSyncFunc;
	}

	free{
		if(traceResponder.notNil, {
			traceResponder.remove;
			traceResponder = nil;
		});

		responder.free;
		responder.clear;
	}

	*prDefaultSpec{
		^\midi.asSpec;
	}

	spec_{|sp|
		spec = sp;
		this.changed(\spec);
	}

	prSetupResponderAndSyncFunc{
		responder = MIDIFunc({arg val, num, chan, src;
			this.valueAction_(val);
			this.changed(\value);
		}, number, chan, msgType, midiIn.uid, argTemplate: argTemplate);
		responder.permanent_(true);
		if(syncFunction.isNil, {
			syncFunction = switch(msgType,
				\control, {
					{arg comp;
						// forkIfNeeded {
							syncCacheValue = value;
							midiOut.control(chan, number, value);
						// };
					};
				},
				\noteOn, {
					{arg comp;
						// forkIfNeeded {
							syncCacheValue = value;
							midiOut.noteOn(chan, number, value);
						// };
					}
				},
				\noteOff, {
					{arg comp;
						// forkIfNeeded {
							syncCacheValue = value;
							midiOut.noteOff(chan, number, value);
						// };
					}
				}
			);
		});
	}

	valueNormalized{
		^spec.unmap(this.value);
	}

	value_{arg val;
		if(val.isNumber, {
			value = val.asInteger;
		});
	}

	valueAction_{arg val;
		value = val;
		this.doAction;
	}

	doAction{
		this.action.value(this);
	}

	update{arg theChanged, what, theChanger;
		if(theChanger !== this, {
			var newVal;
			if(what == \value, {
				newVal = theChanged.spec.unmap(theChanged.value);
				newVal = spec.map(newVal).asInteger;
				value = newVal;
				this.refresh;
				"%\n".postf(newVal);
			});
		});
	}

	refresh{
		syncFunction.value(this);
	}

	trace{arg bool = true;
		if(traceResponder.notNil, {
			traceResponder.remove;
			traceResponder = nil;
		});
		if(bool, {
			traceResponder = SimpleController.new(this).put(\value, {arg comp;
				"%/% - %".format(controllerName, name, comp.value).postln;
			});
		});

	}
}
