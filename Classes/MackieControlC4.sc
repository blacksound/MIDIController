MackieControlC4 {
	var <midiDevice;
	var <name;

	*new{
		|
		inputDeviceName,
		inputPortName,
		outputDeviceName,
		outputPortName,
		name = "Mackie_Control_C4",
		doReset = true
		|
		^super.new.init(
			inputDeviceName, inputPortName,
			outputDeviceName, outputPortName,
			name, doReset
		);
	}

	init{
		|
		inputDeviceName_,
		inputPortName_,
		outputDeviceName_,
		outputPortName_,
		name_,
		doReset = true
		|
		midiDevice = MIDIDevice(
			inputDeviceName_, inputPortName_,
			outputDeviceName_, outputPortName_,
			name_
		);
		name = name_;
		if(doReset, {
			this.reset;
		});

		this.prSetupComponents
	}

	reset{

	}

	free{
		midiDevice.free;
	}

	prSetupComponents{
		var mappings = [
			\split -> (number: 0),
			\spotErase -> (number: 4),
			\lock -> (number: 3),
			\marker -> (number: 5),
			\chanStrip -> (number: 7),
			\track -> (number: 6),
			\function -> (number: 8),
			\shift -> (number: 13),
			\control -> (number: 15),
			\option -> (number: 14),
			\alt -> (number: 16),
			\bankLeft -> (number: 9),
			\bankRight -> (number: 10),
			\singleLeft -> (number: 11),
			\singleRight -> (number: 12),
			\trackLeft -> (number: 19),
			\trackRight -> (number: 20),
			\slotUp -> (number: 17),
			\slotDown -> (number: 18),
		] ++ (32..63).collect({arg num, i;
			Association(
				"encoderButton.%_%".format((i % 8) + 1, "ABCD"[i / 8] ).asSymbol,
				(number: num)
			);
		});
		mappings.do({|mapping|
			var data = mapping.value;
			midiDevice.addComponent("%_press".format(mapping.key).asSymbol, 0, data[\number], \noteOn);
			midiDevice.addComponent("%_release".format(mapping.key).asSymbol, 0, data[\number], \noteOff);
		});

		(0..31).do({arg number, i;
			var col = (i%8) + 1;
			var row = "ABCD"[i / 8].asSymbol;
			var encoderName = "encoder.%_%".format(
				col, row
			).asSymbol;
			midiDevice.addComponent(
				encoderName,
				0,
				number,
				msgType: \increment
			);
		})
	}

	trace{arg val;
		midiDevice.trace(val);
	}

	setLCDString{arg row = 0, line = 0, offset = 0, str;
		var msg = [
			0x30 + row, (0x38 * line) + offset
		] ++ str.ascii;
		this.prSendSysex(msg);
	}

	setStringAtLCDSlot{arg col = 1, row = \A, line = 0, str;
		var r = (A: 0, B: 1, C: 2, D: 3)[row];
		var c = (col - 1)* 7;
		//clear at that slot
		this.setLCDString(r,line,c, "       ");
		this.setLCDString(r,line,c, str.copyRange(0, 6));
	}

	setLEDRing{arg col = 1, row = \A, val, mode = \singleDot, showCenter = false;
		var modes = (boost: 1, wrap: 2, singleDot: 0, spread: 3);
		var r = (A: 0, B: 1, C: 2, D: 3)[row];
		midiDevice.midiOut.control(
			0,
			32.bitOr((r * 8) + (col - 1)),
			(showCenter.asInteger << 6) |
			(modes[mode] << 4) |
			val.clip(0, 11).asInteger
		);
	}

	prSendSysex{arg msg;
		var header = [
			0xF0, 0x00, 0x00, 0x66, 0x17
		];
		var packet = header ++ msg ++ [0xF7];
		midiDevice.midiOut.sysex(packet.as(Int8Array));
	}
}
