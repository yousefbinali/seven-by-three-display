package com.github.yousefbinali.digital.plugin.sevenbythree;

import de.neemann.digital.draw.library.ComponentManager;
import de.neemann.digital.draw.library.ComponentSource;
import de.neemann.digital.draw.library.InvalidNodeException;

public class CustomComponentSource implements ComponentSource
{
    @Override
    public void registerComponents(ComponentManager componentManager) throws InvalidNodeException
    {
        componentManager.addComponent("8-bit Display/", EightBitsToBCDEncoder.DESCRIPTION, ComponentShape::new);
    }
}
