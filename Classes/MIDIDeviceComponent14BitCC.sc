MIDIDeviceComponent14BitCC : MIDIDeviceComponent {
	var hiByteResponder, loByteResponder;
	var loByte, hiByte;
	var waitingForLoByte = false;

	*new{arg midiIn, midiOut, chan, number, argTemplate, name, controllerName;
		^super.new(midiIn, midiOut, chan, number, \control14, argTemplate, name, controllerName);
	}

	prSetupSpec{
		spec = ControlSpec(0, 1023, step: 1, default: 512);
	}

	prSetupResponderAndSyncFunc{
		var calculateValue, loNumber;
		loNumber = number + 32;
		calculateValue = {arg lo, hi;
			var result;
			result = lo + (hi << 7);
			result;
		};
		hiByteResponder = MIDIFunc.cc({arg val, num, chan, src;
			hiByte = val;
			waitingForLoByte = true;
		}, number, chan, midiIn.uid);

		loByteResponder = MIDIFunc.cc({arg val, num, chan, src;
			loByte = val;
			if(waitingForLoByte, {
				var val;
				val = calculateValue.value(loByte, hiByte);
				if(argTemplate.notNil, {
					if(argTemplate.matchItem(val), {
						this.valueAction_(val);
						this.changed(\value);
					});
				}, {
					this.valueAction_(val);
					this.changed(\value);
				});
				waitingForLoByte = false;
				loByte = nil;
				hiByte = nil;
			});
		}, loNumber, chan, midiIn.uid);

		syncFunction = {arg comp;
			fork {
				midiOut.control(chan, number, value >> 7);
				midiOut.control(chan, loNumber, value % 128);
			};
		};
	}

	prCalculateValue{
		value = loByte + (hiByte << 7);
	}

	valueNormalized{
		^this.value.linlin(0, 1023, 0.0, 1.0); //FIXME, this is not really 14 bit, but in most cases its 10bits
	}

}
