MIDIDevice {
	var <midiIn, <midiOut;//temp getters
	var <components;
	var <name;

	*new{arg inDeviceName, inPortName, outDeviceName, outPortName, name;
		^super.new.init(inDeviceName, inPortName, outDeviceName, outPortName, name);
	}

	init{arg inDeviceName, inPortName, outDeviceName, outPortName, name_;
		midiIn = MIDIIn.findPort(inDeviceName, inPortName);
		midiOut = MIDIOut.newByName(outDeviceName, outPortName);
		name = name_;
		components = ();
	}

	setMappings{arg mappingsDict;
		mappingsDict.keysValuesDo({arg key, mappings;
			mappings.do{arg mapping, i;
				var newComp, compName;
				compName = (key ++ "." ++ (i + 1)).asSymbol;
				newComp = MIDIDeviceComponent.create(
					midiIn, midiOut,
					mapping[\chan],
					mapping[\number],
					mapping[\msgType] ? \control,
					compName,
					name
				);
				components.put(compName, newComp);
			};
		});

	}

	addComponent{arg compName, chan, number, msgType = \control;
		var newComp;
		newComp = MIDIDeviceComponent.create(
			midiIn, midiOut, chan, number, msgType, compName, name
		);
		components.put(compName, newComp);
	}

	refresh{
		components.do(_.refresh);
	}

	trace{arg bool = true;
		components.do({arg item; item.trace(bool); });
	}
}
