MIDIDevice {
	var <midiIn, <midiOut;//temp getters
	var <components;
	var <name;

	*new{arg inDeviceName, inPortName, outDeviceName, outPortName, name;
		^super.new.init(inDeviceName, inPortName, outDeviceName, outPortName, name);
	}

	init{arg inDeviceName, inPortName, outDeviceName, outPortName, name_;
		if(MIDIClient.initialized.not, {
			MIDIClient.init;
			MIDIIn.connectAll;
		});
		midiIn = MIDIIn.findPort(inDeviceName, inPortName);
		midiOut = MIDIOut.newByName(outDeviceName, outPortName);
		name = name_;
		components = ();
	}

	free{

	}

	setMappings{arg mappingsDict;
		mappingsDict.keysValuesDo({arg key, mappings;
			mappings.do{arg mapping, i;
				var newComp, compName, number;
				if(mapping.includesKey(\enum), {
					number = mapping[\enum];
				}, {
					number = i + 1;
				});
				compName = (key ++ "." ++ number).asSymbol;
				// "Making component: %".format(compName).postln;
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
//		"Making new component: %".format([compName, chan, number, msgType]).postln;
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
