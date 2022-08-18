package com.github.yousefbinali.digital.plugin.sevenbythree;

import de.neemann.digital.core.Value;
import de.neemann.digital.core.element.*;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.shapes.Interactor;
import de.neemann.digital.draw.shapes.Shape;

import static de.neemann.digital.draw.graphics.Vector.vec;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

import java.awt.*;


public class ComponentShape implements Shape
{
    // g f e d c b a
    // 0 0 0 0 0 0 0
    // lookup table for seven segment display. Segment "a" is at the least significant bit and "g" is at the
    // most significant bit.               0          1          2          3          4           5         6          7          8          9
    private static final int[] LK_TABLE = {0b0111111, 0b0000110, 0b1011011, 0b1001111, 0b1100110, 0b1101101, 0b1111101, 0b0000111, 0b0111111, 0b1101111};
    // seven segments for the ones
    private final Polygon[] ones = {
            Polygon.createFromPath("m 243.45253,-30.333041 0.0597,-20.002 h 80.0229 v 19.82288 z"),                                             // a
            Polygon.createFromPath("m 343.78868,-30.225591 -19.50542,-20.34982 v 81.012939 l 9.74547,9.74547 9.86488,-9.86488 z"),              // b
            Polygon.createFromPath("m 344.1444,110.6831 -19.50543,20.34982 V 50.019978 l 9.74547,-9.74547 9.86489,9.86488 z"),                  // c
            Polygon.createFromPath("m 243.58956,131.37141 0.0597,-20.00199 h 80.0229 v 19.82287 z"),                                            // d
            Polygon.createFromPath("m 223.32221,111.08065 19.50543,20.34982 V 50.417528 l -9.74547,-9.74547 -9.86489,9.86488 z"),               // e
            Polygon.createFromPath("m 223.25692,-30.034861 19.50543,-20.34982 v 81.012939 l -9.74547,9.74547 -9.86488,-9.86488 z"),             // f
            Polygon.createFromPath("m 233.64293,40.387338 10.42823,-10.42823 h 78.99276 l 10.53377,10.53378 -9.64716,9.64716 h -81.18818 z"),   // g
    };

    // seven segments for the tens
    private final Polygon[] tens = {
            Polygon.createFromPath("m 101.25709,-29.995281 0.0597,-20.002 h 80.02291 v 19.82288 z"),                                            // a
            Polygon.createFromPath("m 201.59325,-29.887831 -19.50542,-20.34982 v 81.012939 l 9.74547,9.74547 9.86488,-9.86488 z"),              // b
            Polygon.createFromPath("m 201.94897,111.02086 -19.50543,20.34982 V 50.357738 l 9.74547,-9.74547 9.86489,9.86488 z"),                // c
            Polygon.createFromPath("m 101.39413,131.70917 0.0597,-20.00199 h 80.0229 v 19.82287 z"),                                            // d
            Polygon.createFromPath("M 81.126785,111.41841 100.6322,131.76823 V 50.755288 l -9.745455,-9.74547 -9.86489,9.86488 z"),             // e
            Polygon.createFromPath("m 81.061495,-29.697101 19.505425,-20.34982 v 81.012939 l -9.745465,9.74547 -9.86489,-9.86488 z"),           // f
            Polygon.createFromPath("m 91.447505,40.725098 10.428215,-10.42823 h 78.99277 l 10.53377,10.53378 -9.64716,9.64716 h -81.18818 z"),  // g
    };

    // seven segments for the hundreds
    private final Polygon[] hundreds = {
            Polygon.createFromPath("m -39.855245,-29.955091 0.0597,-20.00199 h 80.0229 v 19.82287 z"),                                          // a
            Polygon.createFromPath("m 60.480915,-29.847641 -19.50543,-20.34982 v 81.012939 l 9.74547,9.74547 9.86488,-9.86488 z"),              // b
            Polygon.createFromPath("m 60.836635,111.06105 -19.50543,20.34982 V 50.397938 l 9.74547,-9.74547 9.86489,9.86488 z"),                // c
            Polygon.createFromPath("m -39.718205,131.74936 0.0597,-20.00199 h 80.02289 v 19.82287 z"),                                          // d
            Polygon.createFromPath("m -59.985565,111.4586 19.50543,20.34982 V 50.795478 l -9.74547,-9.74547 -9.86489,9.86488 z"),               // e
            Polygon.createFromPath("m -60.050845,-29.656901 19.50543,-20.34982 v 81.012929 l -9.74547,9.74547 -9.86489,-9.86488 z"),            // f
            Polygon.createFromPath("m -49.664845,40.765288 10.42823,-10.42823 h 78.99276 l 10.53377,10.53378 -9.64716,9.64716 h -81.18817 z"),  // g
    };

    private final Polygon FRAME = Polygon.createFromPath("M -79.915561,-59.987937 H 360.07375 V 139.97717 H -80.014927 Z");
    private final Style ON, OFF;
    private final int size;
    private final PinDescriptions inputPins;
    private final PinDescriptions outputPins;
    private final Value[] onesBCD = new Value[4];
    private final Value[] tensBCD = new Value[4];
    private final Value[] hundredsBCD = new Value[4];
    private IOState ioState;

    public ComponentShape(ElementAttributes attr, PinDescriptions inputPins, PinDescriptions outputPins)
    {
        ON = Style.NORMAL.deriveFillStyle(attr.get(Keys.COLOR));
        OFF = Style.NORMAL.deriveFillStyle(new Color(169, 169, 169));
        size = attr.get(Keys.SEVEN_SEG_SIZE);
        this.inputPins = inputPins;
        this.outputPins = outputPins;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight)
    {
        Transform transform =  TransformMatrix.scale(0.66f, 0.64f); //createTransform(size);
        graphic.drawPolygon(FRAME.transform(transform), Style.NORMAL);

        long bcd = readBCD(onesBCD);        // reading the four bits from onesBCD into bcd

        for (int i = 0; i < ones.length; i++)
            graphic.drawPolygon(ones[i].transform(transform), getStyle(i, bcd));

        bcd = readBCD(tensBCD);

        for (int i = 0; i < tens.length; i++)
            graphic.drawPolygon(tens[i].transform(transform), getStyle(i, bcd));

        bcd = readBCD(hundredsBCD);
        for (int i = 0; i < hundreds.length; i++)
            graphic.drawPolygon(hundreds[i].transform(transform), getStyle(i, bcd));
    }

    /**
     * Read bits from a separated bcd into an integer.
     * @param bcdList an array containing a bcd.
     * @return a long that contains the bcd.
     */
    long readBCD(Value[] bcdList)
    {
        if (ioState == null || bcdList[0] == null)
            return 0;

        long bcd = 0;

        for (int i = bcdList.length - 1; i > -1; i--) {
            bcd = bcd | bcdList[i].getValue();
            bcd = bcd << 1;
        }

        bcd = bcd >> 1;

        return bcd;
    }

    Style getStyle(int i, long bcd)
    {
        if (isOn(i, bcd))
            return ON;
        else
            return OFF;
    }

    /**
     * isOn will use the segment number and lookup table to check if the segment should be on or off.
     * @param i is the segment number
     * @param bcd binary coded decimal. Will be used as an index for the lookup table.
     * @return true if the segment should be on, and false otherwise.
     */
    boolean isOn(int i, long bcd)
    {
        int segment = 1 << (i);     // move the segment to left i times to compare it with the bit in the lookup table
        return (segment & LK_TABLE[(int) bcd]) > 0;
    }

    @Override
    public Pins getPins()
    {
        Pins pins = new Pins();

        for (int i = 0, j = 7; i < 8 && j > -1; i++, j--)
            pins.add(new Pin(vec(-40 + SIZE * i, -40), inputPins.get(j)));

        for (int i = 0; i < outputPins.size(); i++)
        {
            if (i < 6)
                pins.add(new Pin(vec(120 + SIZE * i, -40), outputPins.get(i)));
            else
                pins.add(new Pin(vec(240, -60 + SIZE * (i - 4)), outputPins.get(i)));
        }

        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState)
    {
        this.ioState = ioState;
        return null;
    }

    /**
     * Since we can not read any inputs or outputs in drawTo method, we can use this method to update our
     * inputs.
     * NOTE: if the program is in edit mode then io state will return null, that is why we check for null.
     */
    @Override
    public void readObservableValues()
    {
        if (ioState != null)
        {
            int outputIndex = 0;

            for (int i = 0; i < onesBCD.length; i++, outputIndex++)
            {
                onesBCD[i] = ioState.getOutput(outputIndex).getCopy();
            }

            for (int i = 0; i < tensBCD.length; i++, outputIndex++)
            {
                tensBCD[i] = ioState.getOutput(outputIndex).getCopy();
            }

            for (int i = 0; i < hundredsBCD.length; i++, outputIndex++)
            {
                hundredsBCD[i] = ioState.getOutput(outputIndex).getCopy();
            }

        }
    }

}
