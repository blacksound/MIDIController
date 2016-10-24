MIDIDeviceComponent14BitCC : MIDIDeviceComponent {
	var hiByteResponder, loByteResponder;
	var loByte, hiByte;
	var waitingForLoByte = false;

	*new{arg midiIn, midiOut, chan, number, name, controllerName;
		^super.new(midiIn, midiOut, chan, number, \control14, name, controllerName);
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
				this.valueAction_(val);
				this.changed(\value);
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
}
