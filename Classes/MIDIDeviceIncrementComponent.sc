MIDIDeviceIncrementComponent : MIDIDeviceComponent {
	var <>incrementFactor = 1;
	var <internalBitResolution = 10;
	var <incrementPowerFactor = 1.3;
	classvar <defaultBitResolution = 10;

	prSetupResponderAndSyncFunc{
		responder = MIDIFunc({arg val, num, chan, src;
			if(val < 64, {
				this.increment(pow(val, incrementPowerFactor));
			}, {
				this.decrement(pow(val - 64, incrementPowerFactor));
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
		this.spec_(this.class.prMakeSpec(val));
	}

	*prMakeSpec{|bitResolution|
		^ControlSpec(0, (2**(bitResolution ? this.defaultBitResolution)).asInteger);
	}

	*prDefaultSpec{
		^this.prMakeSpec(this.defaultBitResolution);
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
