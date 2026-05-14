package datrat.hqmdontvoidmyitems;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
    modid = DontVoidMyItems.MODID,
    name = DontVoidMyItems.NAME,
    version = DontVoidMyItems.VERSION,
    dependencies = "required-after:HardcoreQuesting"
)
public final class DontVoidMyItems {
    public static final String MODID = "hqm-dontvoidmyitems";
    public static final String NAME = "Hardcore Questing Mode: Don't Void My Items";
    public static final String VERSION = "1.0.1";

    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("Loaded {}", NAME);
    }
}
