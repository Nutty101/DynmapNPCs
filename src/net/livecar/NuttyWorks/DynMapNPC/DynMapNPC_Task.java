package net.livecar.NuttyWorks.DynMapNPC;

import java.util.Iterator;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;

public final class DynMapNPC_Task extends BukkitRunnable 
{
    private final DynMapNPC plugin;
    
    public DynMapNPC_Task(DynMapNPC plugin) {
        this.plugin = plugin;
    }
 
    @Override
    public void run() {

    	boolean bDefaultShow = plugin.getConfig().getBoolean("defaults.showonmap",false);
		boolean bNPCFound = false;
    	//Loop the markers and clean out removed NPC's
    	for (Marker oMrkrItem : plugin.cDyn_Markers.getMarkers())
    	{
    		if (oMrkrItem != null)
    		{
    			bNPCFound = false;
    			for (NPC oNPC : CitizensAPI.getNPCRegistry())
    			{
    				if (oNPC != null && ("NPC-ID-" + oNPC.getId()).equalsIgnoreCase(oMrkrItem.getMarkerID()))
    				{
	    				if (oNPC.hasTrait(DynMapNPC_Trait.class)) {
	    					DynMapNPC_Trait trait = oNPC.getTrait(DynMapNPC_Trait.class);
	    					if (trait.markerShowOnMap)
	    					{
    	    					MarkerIcon oIcon = plugin.cDyn_MarkAPI.getMarkerIcon("offlineuser");
            					try{
            						oIcon = plugin.cDyn_MarkAPI.getMarkerIcon(trait.markerIcon);
            					} catch (Exception err)
            					{}
    	    					
    	    					if (trait.markerName != oMrkrItem.getLabel())
    	    						bNPCFound = false;
    	    					if (trait.markerDescription != oMrkrItem.getDescription())
    	    						bNPCFound = false;
    	    					if (oMrkrItem.getMinZoom() != trait.markerMinZoom);
    	    						bNPCFound = false;
    	    					if (oMrkrItem.getMaxZoom() != trait.markerMaxZoom);
    	    						bNPCFound = false;
    	    					if (oMrkrItem.getMarkerIcon().getMarkerIconID() != oIcon.getMarkerIconID())
    	    						bNPCFound = false;
	    						break;
	    					}
	    				} else {
	    					if (bDefaultShow)
	    					{
	    						bNPCFound = true;
	    					}
	    					break;
	    				}
    				}
    			}
    			if (!bNPCFound)
    				oMrkrItem.deleteMarker();
    		}
    	}
    	
    	//Loop NPC's and add a marker for them to the map, or move an existing marker
    	for (Iterator<NPC> oNPCList = CitizensAPI.getNPCRegistry().iterator();oNPCList.hasNext();)
    	{
    		NPC oCurNPC = (NPC)oNPCList.next();
    		if (oCurNPC != null && oCurNPC.getEntity() != null)
    		{
    			bNPCFound = false;
    			Location oNPCLocation = oCurNPC.getEntity().getLocation();
    			
    	    	for (Marker oMrkrItem : plugin.cDyn_Markers.getMarkers())
    	    	{
    	    		if (oMrkrItem != null && oMrkrItem.getMarkerID().equalsIgnoreCase("NPC-ID-" + oCurNPC.getId())){
    	    			//Move the marker
    	    			bNPCFound = true;
    	    			
    	    			if ((oMrkrItem.getX() != oNPCLocation.getX())  || (oMrkrItem.getY() != oNPCLocation.getY()))
    	    			{
    	    					oMrkrItem.setLocation(oNPCLocation.getWorld().getName(), oNPCLocation.getX(), oNPCLocation.getY(), oNPCLocation.getZ());
    	    			}
    	    			break;
    	    		}
    	    	}
    	    	if (!bNPCFound)
    	    	{
    	    		if (oCurNPC.hasTrait(DynMapNPC_Trait.class)) {
    					DynMapNPC_Trait trait = oCurNPC.getTrait(DynMapNPC_Trait.class);
    					if (trait.markerShowOnMap)
    					{
    						String sNPCName = "";

    						if (trait.markerName.trim() == "")
    						{
    							sNPCName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', oCurNPC.getFullName()));
    						} else {
    							sNPCName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', trait.markerName));
    						}
    						
        					MarkerIcon oIcon = plugin.cDyn_MarkAPI.getMarkerIcon("offlineuser");
        					try{
        						oIcon = plugin.cDyn_MarkAPI.getMarkerIcon(trait.markerIcon);
        					} catch (Exception err)
        					{}
        					
    	    	    		Marker oMarker = plugin.cDyn_Markers.createMarker("NPC-ID-" + oCurNPC.getId(),
    	    	    				sNPCName,
    	    	    				false,
    	    	    				oNPCLocation.getWorld().getName().toString(),
    	    	    				oNPCLocation.getX(),
    	    	    				oNPCLocation.getY(),
    	    	    				oNPCLocation.getZ(),
    	    	    				oIcon,
    	    	    				false);
    	    	    		oMarker.setMaxZoom(trait.markerMaxZoom);
    	    	    		oMarker.setMinZoom(trait.markerMinZoom);
    	    	    		
    	    	    		if (plugin.bHyperConomyExists)
    	    	    		{
    	    	    			try {
    	    	    			net.livecar.NuttyWorks.DynMapNPC.HyperConomy.ShopIntegration oIntegrate = new net.livecar.NuttyWorks.DynMapNPC.HyperConomy.ShopIntegration(plugin);
    	    	    			oMarker.setDescription(oIntegrate.GetShop(oCurNPC));
    	    	    			} catch (Exception err)
    	    	    			{
    	    	    				//plugin.getLogger().log(java.util.logging.Level.SEVERE, "Trait " + err.getMessage());
    	    	    			}
    	    	    		} else {
	    	    	    		if (trait.markerDescription.trim() != "")
	    	    	    			oMarker.setDescription(trait.markerDescription);
    						}
    					}
    				} else {
    					if (bDefaultShow)
    					{
	    					//Set the marker to the new location
	    					MarkerIcon oIcon = plugin.cDyn_MarkAPI.getMarkerIcon("offlineuser");
	    					try{
	    						oIcon = plugin.cDyn_MarkAPI.getMarkerIcon(plugin.getConfig().getString("defaults.icon","offlineuser"));
	    					} catch (Exception err)
	    					{}
	    					
	        	    		Marker oMarker = plugin.cDyn_Markers.createMarker("NPC-ID-" + oCurNPC.getId(),
	        	    				ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', oCurNPC.getFullName())),
	        	    				false,
	        	    				oNPCLocation.getWorld().getName().toString(),
	        	    				oNPCLocation.getX(),
	        	    				oNPCLocation.getY(),
	        	    				oNPCLocation.getZ(),
	        	    				oIcon,
	        	    				false);
	        	    		
	        	    		//Use the config defaults
		    	    		oMarker.setMinZoom(plugin.getConfig().getInt("defaults.zoomlevels.min",0));
		    	    		oMarker.setMaxZoom(plugin.getConfig().getInt("defaults.zoomlevels.max",10));
		    	    		
		    	    		if (plugin.getConfig().getBoolean("defaults.showhcinv",false))
		    	    		{
		    	    			if (plugin.bHyperConomyExists)
		    	    			{
			    	    			try {
				    	    			net.livecar.NuttyWorks.DynMapNPC.HyperConomy.ShopIntegration oIntegrate = new net.livecar.NuttyWorks.DynMapNPC.HyperConomy.ShopIntegration(plugin);
				    	    			oMarker.setDescription(oIntegrate.GetShop(oCurNPC));
				    	    		} catch (Exception err)
				    	    		{
		    	    					//plugin.getLogger().log(java.util.logging.Level.SEVERE, "NonTrait " + err.getMessage() + " / " + err.getStackTrace());
				    	    		}
		    	    			} 
	    	    			}
    					}
    				}
    	    	}
    		}
    	}
    }
}
