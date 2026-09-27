package net.matteo.tpsum.utils.maths;

import net.minecraft.util.RandomSource;

public class LocalMath {

    public static final ThreadLocal<RandomSource> random = ThreadLocal.withInitial(RandomSource::create);
}
