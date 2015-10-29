package net.livecar.NuttyWorks.DynMapNPC;

import org.bukkit.Bukkit;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

public class DynMapNPC_Trait extends Trait {

	@Persist public String markerIcon;
	@Persist public String markerName;
	@Persist public String markerDescription;
	@Persist public int markerMinZoom;
	@Persist public int markerMaxZoom;
	@Persist public Boolean markerShowOnMap = false;
	
	DynMapNPC plugin = null;
	
	public DynMapNPC_Trait() {
		super("dynmapnpc");
		plugin = (DynMapNPC) Bukkit.getServer().getPluginManager().getPlugin("DynMapNPC");
	}

	@Override
	public void onAttach() {
		try {
			//Set the defaults for everything
			markerName = "";
			markerDescription = "";
			markerIcon = plugin.getConfig().getString("defaults.icon","offlineuser");
			markerMinZoom = plugin.getConfig().getInt("defaults.zoomlevels.min",10);
			markerMaxZoom = plugin.getConfig().getInt("defaults.zoomlevels.max",0);
			markerShowOnMap = plugin.getConfig().getBoolean("defaults.showonmap",true);
		} catch (Exception err)
		{
			Bukkit.getLogger().log(java.util.logging.Level.INFO, "[DynMapNPCs] Failed to set defaults." + err.getMessage());
		}
	}
}
