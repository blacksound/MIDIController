MIDIDeviceIncrementComponent : MIDIDeviceComponent {
	var <>incrementFactor = 1;
	var internalBitResolution = 10;

	prSetupResponderAndSyncFunc{
		responder = MIDIFunc({arg val, num, chan, src;
			if(val < 64, {
				this.increment(val);
			}, {
				this.decrement(val - 64);
			});
			this.doAction;
		}, number, chan, \control, midiIn.uid, argTemplate: argTemplate);
		responder.permanent_(true);
		if(syncFunction.isNil, {
			syncFunction = {arg comp;
				fork {
					midiOut.control(chan, number, value);
				};
			};
		});
	}

	internalBitResolution_{|val|
		internalBitResolution = val;
		this.prSetupSpec;
	}

	prSetupSpec{
		if(internalBitResolution.notNil, {
			spec = ControlSpec(0, 2**internalBitResolution);
		}, {
			spec = ControlSpec(0, 127);
		});
	}

	increment{|val|
		var delta = max(1, val.squared * incrementFactor);
		this.changed(\increment, delta);
		this.value_(spec.constrain(value + delta));
	}

	decrement{|val|
		var delta = max(1, val.squared * incrementFactor);
		delta = delta.neg;
		this.changed(\increment, delta);
		this.value_(spec.constrain(value + delta));
	}
	
	valueNormalized{
		^spec.unmap(this.value);
	}

	value_{arg val;
		if(val.isNumber, {
			value = val;
			this.changed(\value);
		});
	}

	value{
		^value.asInteger;
	}

	trace{arg bool = true;
		if(traceResponder.notNil, {
			traceResponder.remove;
			traceResponder = nil;
		});
		if(bool, {
			traceResponder = SimpleController.new(this);
			traceResponder.put(\value, {arg comp;
				"%/% - %".format(controllerName, name, comp.value).postln;
			});
			traceResponder.put(\increment, {arg comp, what, delta;
				"%/% increment - %".format(controllerName, name, delta).postln;
			});
		});

	}
}
