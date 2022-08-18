package com.github.yousefbinali.digital.plugin.sevenbythree;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.PinException;

import java.util.ArrayList;

import static de.neemann.digital.core.element.PinInfo.input;

public class EightBitsToBCDEncoder extends Node implements Element
{
    public static final ElementTypeDescription DESCRIPTION =
            new ElementTypeDescription(EightBitsToBCDEncoder.class,
            input("b1","first bit"),
            input("b2","second bit"),
            input("b3","third bit"),
            input("b4","fourth bit"),
            input("b5","fifth bit"),
            input("b6","sixth bit"),
            input("b7","seventh bit"),
            input("b8","eighth bit")) {

        @Override
        public String getDescription(ElementAttributes elementAttributes)
        {
            return "An 8 bit decimal display ";
        }

    }.addAttribute(Keys.ROTATE).addAttribute(Keys.BITS);


    private final int BCD_L     = 4;                // fixed length for bcd
    private final int PINS_L    = 8;                // fixed length for number input pins
    private int[] bcdList;                          // holds the three binary coded decimals after encoding the binary number
    private final int bits;
    private final ObservableValue[] ones;           // outputs four bits with the bcd of the ones position
    private final ObservableValue[] tens;           // outputs four bits with the bcd of the tens position
    private final ObservableValue[] hundreds;       // outputs four bits with the bcd of the hundreds position
    private final ObservableValue[] inPins;
    private final ArrayList<ObservableValue> outputPins = new ArrayList<>();

    public EightBitsToBCDEncoder(ElementAttributes attr)
    {
        bits = attr.getBits();

        ones        = new ObservableValue[BCD_L];
        tens        = new ObservableValue[BCD_L];
        hundreds    = new ObservableValue[BCD_L];
        inPins = new ObservableValue[PINS_L];

        for (int i = 0; i < BCD_L; i++)
        {
            ones[i] = new ObservableValue("o" + (i + 1), bits).setDescription("bit number " + i + " in ones' bcd");
            outputPins.add(ones[i]);
        }

        for (int i = 0; i < BCD_L; i++)
        {
            tens[i] = new ObservableValue("t" + (i + 1), bits).setDescription("bit number " + i + " in tens' bcd");
            outputPins.add(tens[i]);
        }

        for (int i = 0; i < BCD_L; i++)
        {
            hundreds[i] = new ObservableValue("h" + (i + 1), bits).setDescription("bit number " + i + " in hundreds' bcd");
            outputPins.add(hundreds[i]);
        }
    }

    /**
     * This method is called whenever an input value has changed.
     * Writing to output is not allowed!
     */
    @Override
    public void readInputs()
    {
        long binary = 0;         // store the pins values

        // reading every pin input and shifting them left into binary
        for (int i = PINS_L - 1; i > -1; --i)
        {
            long p = inPins[i].getValue();      // reading the least significant bit and going to the next bit in the next iteration
            binary = binary | p;                // assigning the pit into the first bit in binary
            binary = binary << 1;               // shift the assigned bit to the left.
        }

        binary = binary >> 1;
        bcdList = getBcd(binary);
    }

    @Override
    public void writeOutputs() throws NodeException {

        int bcd = bcdList[0];               // getting the ones bcd

        for (int i = 0; i < BCD_L; i++)
        {
            ones[i].setValue(1 & bcd);              // getting the least significant bit
            bcd = bcd >> 1;                         // shifting bits to right to get the next bit
        }

        bcd = bcdList[1];
        for (int i = 0; i < BCD_L; i++)
        {
            tens[i].setValue(1 & bcd);              // getting the least significant bit
            bcd = bcd >> 1;                         // shifting bits to right to get the next bit
        }

        bcd = bcdList[2];
        for (int i = 0; i < BCD_L; i++)
        {
            hundreds[i].setValue(1 & bcd);              // getting the least significant bit
            bcd = bcd >> 1;                         // shifting bits to right to get the next bit
        }
    }

    /**
     * Called to register input signals.
     * @param inputs    list of ObservableValue
     * @throws NodeException Not thrown
     */
    @Override
    public void setInputs(ObservableValues inputs) throws NodeException
    {
        for (int i = 0; i < PINS_L; i++)
            inPins[i] = inputs.get(i).addObserverToValue(this).checkBits(bits, this);
    }

    /**
     * A method for converting 8-bit binary to binary coded decimal.
     * @param binary binary number to be encoded to bcd
     * @return an array containing the bcd starting from index 0 = ones, index 1 = tens, etc.
     */
    private static int[] getBcd(long binary)
    {
        int[] bcdNumbers    = new int[(8 + 2) / 3];    // number of bits for all output bcd

        for (int i = 0; i < bcdNumbers.length && binary > 0; i++)
        {
            bcdNumbers[i] = (int) binary % 10;
            binary /= 10;
        }

        return bcdNumbers;
    }

    @Override
    public ObservableValues getOutputs() throws PinException {
        return new ObservableValues(outputPins);
    }
}
