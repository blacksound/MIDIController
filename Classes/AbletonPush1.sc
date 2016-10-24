AbletonPush1 {
	var <userDevice;
	var <liveDevice;
	var <name;

	*new{
		arg userInputDeviceName = "Ableton Push",
		userInputPortName = "User Port",
		userOutputDeviceName = "Ableton Push",
	       	userOutputPortName = "User Port",
	       	liveInputDeviceName = "Ableton Push",
	       	liveInputPortName = "Live Port",
	       	liveOutputDeviceName = "Ableton Push",
	       	liveOutputPortName = "Live Port",
		name = "Push";
		^super.new.init(
			userInputDeviceName, userInputPortName,
			userOutputDeviceName, userOutputPortName,
			liveInputDeviceName, liveInputPortName,
			liveOutputDeviceName, liveOutputPortName, name;
		);
	}

	init{
		arg userInputDeviceName_,
		userInputPortName_,
		userOutputDeviceName_,
	       	userOutputPortName_,
	       	liveInputDeviceName_,
	       	liveInputPortName_,
	       	liveOutputDeviceName_,
	       	liveOutputPortName_,
		name_;

		userDevice = MIDIDevice(
			userInputDeviceName_, userInputPortName_,
		       	userOutputDeviceName_, userOutputPortName_,
			name_ ++ "User"
		);
		liveDevice = MIDIDevice(
			liveInputDeviceName_, liveInputPortName_,
		       	liveOutputDeviceName_, liveOutputPortName_,
			name_ ++ "Live"
		);
		name = name_;
		this.prSetupUserPortMappings;
		this.prSetupComponentLEDMethods;
	}
	
	prSetupUserPortMappings{
		[
			(1..8).collect({arg channelNumber, i;
				[
					\trackSelect -> (number: (20..27)),
					\sceneLaunch -> (number: (36..43)),
					\encoder -> (number: (71..78)),
					\trackState -> (number: (102..109))
				].collect({arg it;
					"%.%".format(it.key, channelNumber).asSymbol -> (number: it.value[\number][i])
				})
			}),
			\tapTempo -> (number: 3),
			\metronome -> (number: 9),
			\tempoEncoder -> (number: 14),
			\swingEncoder -> (number: 15),
			\masterSelect -> (number: 28),
			\trackStop -> (number: 29),
			\leftArrow -> (number: 44),
			\rightArrow -> (number: 45),
			\upArrow -> (number: 46),
			\downArrow -> (number: 47),
			\select -> (number: 48),
			\shift -> (number: 49),
			\noteMode -> (number: 50),
			\sessionMode -> (number: 51),
			\addDevice -> (number: 52),
			\addTrack -> (number: 53),
			\octaveDown -> (number: 54),
			\octaveUp -> (number: 55),
			\repeat -> (number: 56),
			\accent -> (number: 57),
			\scales -> (number: 58),
			\mute -> (number: 60),
			\solo -> (number: 61),
			\in -> (number: 62),
			\out -> (number: 63),
			\masterEncoder -> (number: 79),
			\play -> (number: 85),
			\record -> (number: 86),
			\new -> (number: 87),
			\duplicate -> (number: 88),
			\automation -> (number: 89),
			\fixedLength -> (number: 90),
			\deviceMode -> (number: 110),
			\browseMode -> (number: 111),
			\trackMode -> (number: 112),
			\clipMode -> (number: 113),
			\volumeMode -> (number: 114),
			\panAndSendMode -> (number: 115),
			\quantize -> (number: 116),
			\double -> (number: 117),
			\delete -> (number: 118),
			\undo -> (number: 119)
		].flat.collect({arg assoc;
			assoc.key -> (assoc.value ++ (chan: 0, msgType: \control));
		}).do({arg mapping;
			var data = mapping.value;
			userDevice.addComponent(mapping.key, data[\chan], data[\number], data[\msgType]);
		});
		[
			(1..8).collect({arg channelNumber, i;
				[
					\encoderTouch -> (number: (0..7)),
				].collect({arg it;
					"%.%".format(it.key, channelNumber).asSymbol -> (number: it.value[\number][i])
				}).flat
			}).flat,
			(1..8).collect({arg columnNumber, columnIndex;
				[\A, \B, \C, \D, \E, \F, \G, \H].collect({arg rowLetter, rowIndex;
					"pad.%/%".format(columnNumber, rowLetter).asSymbol -> (number: (rowIndex + (columnIndex * 8)) + 36);
				});
			}),
			\masterEncoderTouch -> (number: 8),
			\swingEncoderTouch -> (number: 9),
			\tempoEncoderTouch -> (number: 10),
			\touchStripTouch -> (number: 12)
		].flat.collect({arg assoc;
			[
				"%/on".format(assoc.key).asSymbol -> (assoc.value ++ (chan: 0, msgType: \noteOn)),
				"%/off".format(assoc.key).asSymbol -> (assoc.value ++ (chan: 0, msgType: \noteOff))
			];
		}).flat.do({arg mapping;
			var data = mapping.value;
			userDevice.addComponent(mapping.key, data[\chan], data[\number], data[\msgType]);
		});
		userDevice.addComponent('padChannelPressure', 0, 0, \touch);
		userDevice.addComponent('touchStrip', 0, 0, \bend);
		(1..8).collect({arg columnNumber, columnIndex;
			[\A, \B, \C, \D, \E, \F, \G, \H].collect({arg rowLetter, rowIndex;
				"padPressure.%/%".format(columnNumber, rowLetter).asSymbol -> (number: (rowIndex + (columnIndex * 8)) + 36);
			});
		}).flat.do({arg mapping;
			var data = mapping.value;
			userDevice.addComponent(mapping.key, 0, data[\number], \polytouch)
		});
	}

	prSetupComponentLEDMethods{
		[
			\tapTempo, \metronome, \tempoEncoder, \swingEncoder, \masterSelect, \trackStop,
			\leftArrow, \rightArrow, \upArrow, \downArrow, \select, \shift, \noteMode,
			\sessionMode, \addDevice, \addTrack, \octaveDown, \octaveUp, \repeat, \accent,
			\scales, \mute, \solo, \in, \out, \masterEncoder, \play, \record, \new,
			\duplicate, \automation, \fixedLength, \deviceMode, \browseMode, \trackMode, \clipMode,
			\volumeMode, \panAndSendMode, \quantize, \double, \delete, \undo
		].flat.do({arg compName;
			userDevice.components[compName].addUniqueMethod(\setLED, {arg self, val, blinking = \none;
				var ccVal, ccNum;
				if(val == \off, {
					ccVal = 0;
				}, {
					ccVal = (half: 1, full: 4).at(val) + (none: 0, slow: 1, fast: 2).at(blinking);
				});
				self.midiOut.control(0, self.number, ccVal);
			});
		});
		(1..8).do({arg item;
			[\sceneLaunch, \trackSelect].do({arg buttonName;
				this.components["%.%".format(buttonName, item).asSymbol].addUniqueMethod(\setLED,
					{arg self, val, color, blinking;
						var ccVal;
						if(val == \off, {
							ccVal = 0;
						}, {
							ccVal = (red: 1, amber: 7, yellow: 13, green: 19).at(color) + 
							(half: 0, full: 3).at(val) +
							(none: 0, slow: 1, fast: 2).at(blinking);
						});
						self.midiOut.control(0, self.number, ccVal);
					}
				);
			});
		});
	}

	components{ ^userDevice.components; }

	trace{arg val;
		userDevice.trace(val);
	}
	
	setMode{arg mode;
		var num = (user: 0, live: 1).at(mode);
		liveDevice.midiOut.sysex(Int8Array[240, 71, 127, 21, 98, 0, 1, num, 247]);
	}

	setPressureMode{arg mode;
		var num = (poly: 0, channel: 1).at(mode);
		liveDevice.midiOut.sysex(Int8Array[240, 71, 127, 21, 92, 0, 1, num, 247]);
	}

	setLCDLineString{arg lineNumber, string;
		var header = [240, 71, 127, 21, lineNumber + 23, 0, 69, 0];
		var val = header ++ string.padRight(68).ascii ++ [247];
		val = val.as(Int8Array);
		liveDevice.midiOut.sysex(val);
	}

	clearLCDLine{arg lineNumber;
		var val = [240, 71, 127, 21, lineNumber + 27, 0, 0, 247].as(Int8Array);
		liveDevice.midiOut.sysex(val);
	}

	setPadLEDColor{arg row, column, color;
		var padNumber = (column - 1) + ((row - 1) * 8);
		var redHi, redLo, greenHi, greenLo, blueHi, blueLo;
		var bytes = (color.asArray * 255).asInteger;
		redHi = bytes[0] >> 4;
		redLo = bytes[0].bitAnd(15);
		greenHi = bytes[1] >> 4;
		greenLo = bytes[1].bitAnd(15);
		blueHi = bytes[2] >> 4;
		blueLo = bytes[2].bitAnd(15);
		bytes = [240, 71, 127, 21, 4, 0, 8, padNumber, 0, redHi, redLo, greenHi, greenLo, blueHi, blueLo, 247];
		bytes = bytes.as(Int8Array);
		userDevice.midiOut.sysex(bytes);
	}

	setTrackStateLEDColor{arg buttonNumber, color;
		this.setPadLEDColor(9, buttonNumber, color);
	}

	setButtonLED{arg buttonName, val, blinking = \none;
		userDevice.components[buttonName].setLED(val, blinking);
	}

	setSceneLaunchLED{arg buttonNumber, val, color, blinking = \none;
		this.components["sceneLaunch.%".format(buttonNumber).asSymbol].setLED(val, color, blinking);
	}

	setTrackSelectLED{arg buttonNumber, val, color, blinking = \none;
		this.components["trackSelect.%".format(buttonNumber).asSymbol].setLED(val, color, blinking);
	}
}
