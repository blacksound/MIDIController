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
		if(thisProcess.platform.name == \linux, {
			var outIndex;
			outIndex = MIDIClient.destinations.detectIndex({|source|
				source.uid == midiOut.uid;
			});
			if(outIndex.notNil, {
				midiOut.connect(outIndex);
			}, {
				"Could not connect MIDIOut to virtual source: %".format(
					midiOut
				).warn;
			});
		});
		name = name_;
		components = ();
	}

	free{
		components.keysValuesDo({|compKey, comp|
			comp.free;
		})
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
				this.addComponent(
					compName: compName,
					chan: mapping[\chan],
					number: mapping[\number],
					msgType: mapping[\msgType],
					argTemplate: mapping[\argTemplate],
					spec: mapping[\spec],
					syncFunction: mapping[\syncFunction]
				);
			};
		});
	}

	addComponent{arg compName, chan, number, msgType = \control, argTemplate, spec, syncFunction;
		var newComp;
		newComp = MIDIDeviceComponent.create(
			midiIn, midiOut, chan, number, msgType, argTemplate, compName, name, spec, syncFunction
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
