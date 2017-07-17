package net.livecar.NuttyWorks.DynMapNPC;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

public class DynMapNPC extends org.bukkit.plugin.java.JavaPlugin implements org.bukkit.event.Listener 
{
	//Mappings to Dynmap
	public DynmapAPI cDyn_Plugin;
	public MarkerAPI cDyn_MarkAPI;
	public MarkerSet cDyn_Markers;
	public String Version = "0.1";
	private BukkitTask cProcessing_Task;
	public boolean bHyperConomyExists;
	
	public void onEnable() 
	{
	
		if(getServer().getPluginManager().getPlugin("Citizens") == null || getServer().getPluginManager().getPlugin("Citizens").isEnabled() == false) {
			getLogger().log(java.util.logging.Level.SEVERE, "Citizens 2.0 not found or not enabled");
			getServer().getPluginManager().disablePlugin(this);	
			return;
		}	

		if(getServer().getPluginManager().getPlugin("dynmap") == null || getServer().getPluginManager().getPlugin("dynmap").isEnabled() == false) {
			getLogger().log(java.util.logging.Level.SEVERE, "dynmap not found or not enabled");
			getServer().getPluginManager().disablePlugin(this);	
		}	

		if(getServer().getPluginManager().getPlugin("HyperConomy") == null) {
			getLogger().log(java.util.logging.Level.INFO, "Hyperconomoy Not found");
			bHyperConomyExists = false;
		}	else {
			if(getServer().getPluginManager().getPlugin("HyperMerchant") == null) {
				getLogger().log(java.util.logging.Level.INFO, "HyperMerchant Not found");
				bHyperConomyExists = false;
			} else {
				getLogger().log(java.util.logging.Level.INFO,  "Hyperconomy / Hypermerchant found. Enabled");
				bHyperConomyExists = true;
			}
		}

		SetupConfig();
		
		getLogger().log(java.util.logging.Level.INFO, "Connecting to DynMap");

		try {
			SetupDynMap();
		} catch (Exception err)
		{
			getLogger().log(java.util.logging.Level.INFO, "Errors while connecting to the Dynmap API");
			getServer().getPluginManager().disablePlugin(this);	
			cDyn_Plugin = null;
			cDyn_MarkAPI = null;
			cDyn_Markers = null;
			return;
		}
		
		//Setup the Citizens NPC link
		net.citizensnpcs.api.CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(DynMapNPC_Trait.class).withName("dynmapnpc"));
		
		//Schedule the main thread to monitor for NPCs
		cProcessing_Task = new DynMapNPC_Task(this).runTaskTimer(this, this.getConfig().getLong("interval",100L), this.getConfig().getLong("interval",100L));
		
		if (this.getConfig().getBoolean("mcstats",true))
		{
			try {
		        MCStatsMetrics metrics = new MCStatsMetrics(this);
		        metrics.start();
		    } catch (Exception e) {
		        // Failed to submit the stats :-(
		    }
		}
		
	}
	public void onDisable() 
	{
		Bukkit.getServer().getScheduler().cancelTasks(this);
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] inargs) 
	{

		if (inargs.length == 0 || inargs[0].equalsIgnoreCase("help")) {
			sender.sendMessage(ChatColor.GOLD + "----- DynMapNPC Help [" + ChatColor.GREEN + "V" + this.getDescription().getVersion() + ChatColor.GOLD +"]-----");
			if (sender.hasPermission("dynmapnpc.info") || sender.isOp()) {
				sender.sendMessage(ChatColor.GOLD + "settings " + ChatColor.RED + "-- Display current defaults");
				sender.sendMessage(ChatColor.GOLD + "npc      " + ChatColor.RED + "-- Display settings on the selected NPC");
			}
			if (sender.hasPermission("dynmapnpc.settings") || sender.isOp()) 
			{
				sender.sendMessage(ChatColor.GOLD + "----- DynMapNPC General Commands [" + ChatColor.GREEN + "/dmnpc" + ChatColor.GOLD + "] Help -----");
				
				sender.sendMessage(ChatColor.GOLD + "Toggle " + ChatColor.RED +"   -- Toggle showing NPC's on or off ");
				sender.sendMessage(ChatColor.GOLD + "reload " + ChatColor.RED +"   -- Reload the config file (-s saves first)");

				sender.sendMessage(ChatColor.GOLD + "----- DynMapNPC Config Commands [" + ChatColor.GREEN + "/dmnpcd" + ChatColor.GOLD + "] Help -----");
			
				sender.sendMessage(ChatColor.GOLD + "icon " + ChatColor.GREEN + "<IconName>  " + ChatColor.RED +"-- Icon to display ");
				sender.sendMessage(ChatColor.GOLD + "minzoom " + ChatColor.GREEN + "<10-0>   " + ChatColor.RED +"-- Minimum Zoom to show this NPC");
				sender.sendMessage(ChatColor.GOLD + "maxzoom " + ChatColor.GREEN + "<10-0>  " + ChatColor.RED +"-- Maximum Zoom to show this NPC");
				sender.sendMessage(ChatColor.GOLD + "showonmap " + ChatColor.RED + "        -- Toggle NPC's Visiblility");
				sender.sendMessage(ChatColor.GOLD + "showhcinv " + ChatColor.RED + "        -- Toggle Hyperconomy shop info");
				sender.sendMessage(ChatColor.GOLD + "----- DynMapNPC NPC Subcommands [" + ChatColor.GREEN + "/dmnpcn" + ChatColor.GOLD + "] Help -----");
				
				sender.sendMessage(ChatColor.GOLD + "Use  " + ChatColor.GREEN + "/trait dynmapnpc " + ChatColor.GOLD +" to attach this to a citizen");
				sender.sendMessage(ChatColor.GOLD + "icon " + ChatColor.GREEN + "<IconName>   " + ChatColor.RED +" -- Icon to display ");
				sender.sendMessage(ChatColor.GOLD + "name " + ChatColor.GREEN + "<NPC Name>   " + ChatColor.RED +" -- Text displayed on map ");
				sender.sendMessage(ChatColor.GOLD + "desc" + ChatColor.GREEN + "<Description> " + ChatColor.RED +" -- Secondary text to show");
				sender.sendMessage(ChatColor.GOLD + "minzoom " + ChatColor.GREEN + "<10-0>    " + ChatColor.RED +" -- Minimum Zoom to show this NPC");
				sender.sendMessage(ChatColor.GOLD + "maxzoom " + ChatColor.GREEN + "<10-0>    " + ChatColor.RED +" -- Maximum Zoom to show this NPC");
				sender.sendMessage(ChatColor.GOLD + "showonmap " + ChatColor.RED +"          -- Toggle NPC's Visiblility");
			}
		    return true;
		}
	
		if (cmd.getName().equalsIgnoreCase("dmnpc"))
		{
			if (inargs[0].equalsIgnoreCase("toggle")) 
			{
				if (!sender.hasPermission("dynmapnpc.settings") && !sender.isOp()) {
					sender.sendMessage(ChatColor.DARK_RED + "You do not have permission");
					return true;
				} else {
					if (cProcessing_Task == null)
					{
						//Schedule the main thread to monitor for NPCs
						cProcessing_Task = new DynMapNPC_Task(this).runTaskTimer(this, this.getConfig().getLong("interval",100L), this.getConfig().getLong("interval",100L));
						sender.sendMessage(ChatColor.GREEN + " NPC Processing has been " + ChatColor.YELLOW + "Started");
					} else {
						cProcessing_Task.cancel();
						cProcessing_Task = null;
						sender.sendMessage(ChatColor.GREEN + " NPC Processing has been " + ChatColor.RED + "Stopped");
					}
					return true;
				}
			}
			if (inargs[0].equalsIgnoreCase("reload")) 
			{
				if (!sender.hasPermission("dynmapnpc.settings") && !sender.isOp()) {
					sender.sendMessage(ChatColor.DARK_RED + "You do not have permission");
					return true;
				} else {
					if (inargs.length > 1)
					{
						if (inargs[1].equalsIgnoreCase("-s"))
						{
							this.saveConfig();
							sender.sendMessage(ChatColor.GREEN + " Config file  " + ChatColor.YELLOW + "Saved");
						}
					}
					this.reloadConfig();
				
					sender.sendMessage(ChatColor.GREEN + " Config file  " + ChatColor.YELLOW + "reloaded");
					return true;
				}
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("dmnpcd"))
		{
			if (inargs[0].equalsIgnoreCase("settings")) 
			{
				if (!sender.hasPermission("dynmapnpc.info") && !sender.isOp()) {
					sender.sendMessage(ChatColor.DARK_RED + "You do not have permission");
					return true;
				} else {
					sender.sendMessage(ChatColor.GOLD + "----- DynMapNPC Default Settings -----");
					sender.sendMessage(ChatColor.GREEN + "MarkersetName:   " + ChatColor.YELLOW + " " + this.getConfig().getString("markerset_name","DynMapNPCs"));
					sender.sendMessage(ChatColor.GREEN + "Update Interval: " + ChatColor.YELLOW + " " + this.getConfig().getString("interval","100"));
					sender.sendMessage(ChatColor.GOLD + "----- DynMapNPC NPC Default Settings -----");
					sender.sendMessage(ChatColor.GREEN + "Icon:      " + ChatColor.YELLOW + " " + this.getConfig().getString("defaults.icon","offlineuser"));
					sender.sendMessage(ChatColor.GREEN + "ShowOnMap: " + ChatColor.YELLOW + " " + this.getConfig().getString("defaults.showonmap","true"));
					sender.sendMessage(ChatColor.GREEN + "Min Zoom:  " + ChatColor.YELLOW + " " + this.getConfig().getString("defaults.zoomlevels.min","10"));
					sender.sendMessage(ChatColor.GREEN + "Max Zoom:  " + ChatColor.YELLOW + " " + this.getConfig().getString("defaults.zoomlevels.max","1"));
					return true;
				}
			}
			if (inargs[0].equalsIgnoreCase("icon")) 
			{
				if (!sender.hasPermission("dynmapnpc.settings") || !sender.isOp()) {
					sender.sendMessage(ChatColor.DARK_RED + "You do not have permission");
					return true;
				} else {
					if (inargs.length == 2)
					{
						this.getConfig().set("defaults.icon", inargs[1]);
						sender.sendMessage(ChatColor.GREEN + "Default Icon: Set to " + ChatColor.YELLOW + inargs[1]);	
					} else {
						sender.sendMessage(ChatColor.GREEN + " Icon Name needed " + ChatColor.YELLOW + "Usage: " + ChatColor.RED + "/dnpcd icon <icon name>");
					}
					this.saveConfig();
					return true;
				}
			}
			if (inargs[0].equalsIgnoreCase("minzoom")) 
			{
				if (!sender.hasPermission("dynmapnpc.settings") && !sender.isOp()) {
					sender.sendMessage(ChatColor.DARK_RED + "You do not have permission");
					return true;
				} else {
					if (inargs.length == 2)
					{
						this.getConfig().set("defaults.zoomlevels.min", Integer.parseInt(inargs[1]));
						sender.sendMessage(ChatColor.GREEN + "Default MinZoom: Set to " + ChatColor.YELLOW + inargs[1]);	
					} else {
						sender.sendMessage(ChatColor.GREEN + "Zoom level needed  " + ChatColor.YELLOW + "Usage: " + ChatColor.RED + "/dnpcd minzoom <0-10>");
					}
					this.saveConfig();
					return true;
				}
			}	
			if (inargs[0].equalsIgnoreCase("maxzoom")) 
			{
				if (!sender.hasPermission("dynmapnpc.settings") && !sender.isOp()) {
					sender.sendMessage(ChatColor.DARK_RED + "You do not have permission");
					return true;
				} else {
					if (inargs.length == 2)
					{
						this.getConfig().set("defaults.zoomlevels.max", Integer.parseInt(inargs[1]));
						sender.sendMessage(ChatColor.GREEN + "Default MaxZoom: Set to " + ChatColor.YELLOW + inargs[1]);	
					} else {
						sender.sendMessage(ChatColor.GREEN + "Zoom level needed  " + ChatColor.YELLOW + "Usage: " + ChatColor.RED + "/dnpcd mxnzoom <0-10>");
					}
					this.saveConfig();
					return true;
				}
			}
			if (inargs[0].equalsIgnoreCase("showonmap")) 
			{
				if (!sender.hasPermission("dynmapnpc.settings") && !sender.isOp()) {
					sender.sendMessage(ChatColor.DARK_RED + "You do not have permission");
					return true;
				} else {
					if (this.getConfig().getBoolean("defaults.showonmap",true))
					{
						sender.sendMessage(ChatColor.GREEN + "Default Showonmap: Set to " + ChatColor.YELLOW + "false");
						this.getConfig().set("defaults.showonmap",false);
					} else {
						sender.sendMessage(ChatColor.GREEN + "Default Showonmap: Set to " + ChatColor.YELLOW + "true");
						this.getConfig().set("defaults.showonmap",true);
					}
					this.saveConfig();
					return true;	
				}
			}	
			if (inargs[0].equalsIgnoreCase("showhcinv")) 
			{
				if (!sender.hasPermission("dynmapnpc.settings") && !sender.isOp()) {
					sender.sendMessage(ChatColor.DARK_RED + "You do not have permission");
					return true;
				} else {
					if (this.getConfig().getBoolean("defaults.showhcinv",true))
					{
						sender.sendMessage(ChatColor.GREEN + "Default showhcinv: Set to " + ChatColor.YELLOW + "false");
						this.getConfig().set("defaults.showhcinv",false);
					} else {
						sender.sendMessage(ChatColor.GREEN + "Default showhcinv: Set to " + ChatColor.YELLOW + "true");
						this.getConfig().set("defaults.showhcinv",true);
					}
					this.saveConfig();
					return true;	
				}
			}	
		}
		if(!(sender instanceof Player))
		{
			sender.sendMessage("The command you used either does not exist, or is not available from the console.");
			return true;
		}
		
		Player player = (Player)sender;

		//Citizens default code, left mostly intact
		
		//This block of code will allow your users to specify 
		//The first will run the command on the selected NPC, the second on the NPC with npcID #.
		int npcid = -1;
		int i = 0;
		//did player specify a id?
		try{
			npcid = Integer.parseInt(inargs[0]);
			i = 1;
		}
		catch(Exception e){
		}	
		String[] args = new String[inargs.length-i];
		for (int j = i; j < inargs.length; j++) {
			args[j-i] = inargs[j];
		}
	
	
		//Now lets find the NPC this should run on.
		NPC npc;
		if (npcid == -1){
			npc =	((Citizens)	this.getServer().getPluginManager().getPlugin("Citizens")).getNPCSelector().getSelected(sender);
			if(npc != null ){
				// Gets NPC Selected for this sender
				npcid = npc.getId();
			}
			else{
				//no NPC selected.
				sender.sendMessage(ChatColor.RED + "You must have a NPC selected to use this command");
				return true;
			}			
		}
	
		npc = CitizensAPI.getNPCRegistry().getById(npcid); 
		if (npc == null) {
			//specified number doesn't exist.
			sender.sendMessage(ChatColor.RED + "NPC with id " + npcid + " not found");
			return true;
		}
	
	
		//	If you need access to the instance of MyTrait on the npc, get it like this
		DynMapNPC_Trait trait =null;
		if (!npc.hasTrait(DynMapNPC_Trait.class)) {
			sender.sendMessage(ChatColor.RED + "That command must be performed on a npc with trait: DynMapNPC");
			return true;
		}
		else trait = npc.getTrait(DynMapNPC_Trait.class);

		if (inargs[0].equalsIgnoreCase("npc"))
		{
			if (!player.hasPermission("dynmapnpc.info")) {
				player.sendMessage(ChatColor.DARK_RED + "You do not have permission");
				return true;
			} else {
				sender.sendMessage(ChatColor.GOLD + "----- DynMapNPC Settings -----");
				sender.sendMessage(ChatColor.GREEN + "Icon:        " + ChatColor.YELLOW + " " + trait.markerIcon);
				sender.sendMessage(ChatColor.GREEN + "Name:        " + ChatColor.YELLOW + " " + trait.markerName);
				sender.sendMessage(ChatColor.GREEN + "Description: " + ChatColor.YELLOW + " " + trait.markerDescription);
				sender.sendMessage(ChatColor.GREEN + "Min Zoom:    " + ChatColor.YELLOW + " " + trait.markerMinZoom);
				sender.sendMessage(ChatColor.GREEN + "Max Zoom:    " + ChatColor.YELLOW + " " + trait.markerMaxZoom);
				sender.sendMessage(ChatColor.GREEN + "Visible:     "+ ChatColor.YELLOW + " " + (trait.markerShowOnMap?"Showing on DynMap":"Not Shown On Map"));;
				return true;
			}
		}

		
		if (cmd.getName().equalsIgnoreCase("dmnpcn"))
		{
			if (inargs[0].equalsIgnoreCase("icon")) 
			{
				if (!player.hasPermission("dynmapnpc.settings")) {
					player.sendMessage(ChatColor.DARK_RED + "You do not have permission");
					return true;
				} else {
					if (inargs.length == 2)
					{
						for (MarkerIcon oMrkrIcon: this.cDyn_MarkAPI.getMarkerIcons())
						{
							if (oMrkrIcon.getMarkerIconID().toString().equalsIgnoreCase(inargs[1]))
							{
								//Found
								trait.markerIcon = inargs[1];
								player.sendMessage(ChatColor.GREEN + "Icon: Set to " + ChatColor.YELLOW + inargs[1]);
								return true;
							}
						}
						player.sendMessage(ChatColor.GREEN + "Icon: Icon does not exist in dynmap " + ChatColor.YELLOW + inargs[1]);
						return true;
					} else {
						player.sendMessage(ChatColor.GREEN + "con Name needed " + ChatColor.YELLOW + "Usage: " + ChatColor.RED + "/dnpcn icon <icon name>");
					}
					return true;
				}
			}
			if (inargs[0].equalsIgnoreCase("minzoom")) 
			{
				if (!player.hasPermission("dynmapnpc.settings")) {
					player.sendMessage(ChatColor.DARK_RED + "You do not have permission");
					return true;
				} else {
					if (inargs.length == 2)
					{
						try {
							trait.markerMinZoom = Integer.parseInt(inargs[1]);
							player.sendMessage(ChatColor.GREEN + "MinZoom: Set to " + ChatColor.YELLOW + inargs[1]);
							return true;
						} catch (Exception err)
						{}
					}
					player.sendMessage(ChatColor.GREEN + "Zoom level needed  " + ChatColor.YELLOW + "Usage: " + ChatColor.RED + "/dnpcn minzoom <0-10>");
					return true;
				}
			}	
			if (inargs[0].equalsIgnoreCase("maxzoom")) 
			{
				if (!player.hasPermission("dynmapnpc.settings")) {
					player.sendMessage(ChatColor.DARK_RED + "You do not have permission");
					return true;
				} else {
					if (inargs.length == 2)
					{
						try {
							trait.markerMaxZoom = Integer.parseInt(inargs[1]);
							player.sendMessage(ChatColor.GREEN + "MaxZoom: Set to " + ChatColor.YELLOW + inargs[1]);
							return true;
						} catch (Exception err)
						{}
					}
					player.sendMessage(ChatColor.GREEN + "Zoom level needed  " + ChatColor.YELLOW + "Usage: " + ChatColor.RED + "/dnpcn maxzoom <0-10>");
					return true;
				}
			}	
			if (inargs[0].equalsIgnoreCase("name")) 
			{
				if (!player.hasPermission("dynmapnpc.settings")) {
					player.sendMessage(ChatColor.DARK_RED + "You do not have permission");
					return true;
				} else {
					if (inargs.length == 2)
					{
							trait.markerName = inargs[1];
							player.sendMessage(ChatColor.GREEN + "Name: Set to " + ChatColor.YELLOW + inargs[1]);
					} else {
						player.sendMessage(ChatColor.GREEN + "Name needed " + ChatColor.YELLOW + "Usage: " + ChatColor.RED + "/dnpcn name <Display Name>");
					}
					return true;
				}
			}
			if (inargs[0].equalsIgnoreCase("desc")) 
			{
				if (!player.hasPermission("dynmapnpc.settings")) {
					player.sendMessage(ChatColor.DARK_RED + "You do not have permission");
					return true;
				} else {
					if (inargs.length == 2)
					{
							trait.markerDescription = inargs[1];
							player.sendMessage(ChatColor.GREEN + "Description: Set to " + ChatColor.YELLOW + inargs[1]);
					} else {
						player.sendMessage(ChatColor.GREEN + "Name needed " + ChatColor.YELLOW + "Usage: " + ChatColor.RED + "/dnpcn desc <Description>");
					}
					return true;
				}
			}
			if (inargs[0].equalsIgnoreCase("showonmap"))
			{
				if (!player.hasPermission("dynmapnpc.settings")) {
					player.sendMessage(ChatColor.DARK_RED + "You do not have permission");
					return true;
				} else {
					if (trait.markerShowOnMap)
					{
						player.sendMessage(ChatColor.GREEN + "Showonmap: Set to " + ChatColor.YELLOW + "false");
					} else {
						player.sendMessage(ChatColor.GREEN + "Showonmap: Set to " + ChatColor.YELLOW + "true");
					}
					trait.markerShowOnMap = !trait.markerShowOnMap;
					return true;
				}
			}
		}
		
		player.sendMessage(ChatColor.RED + "invalid command. try help");	
		return true; // do this if you didn't handle the command.
	}
	
	private void SetupDynMap()
	{
		//Pull references to Dynmap
		if (cDyn_Plugin == null)
			cDyn_Plugin = (DynmapAPI)getServer().getPluginManager().getPlugin("dynmap");
		if (cDyn_MarkAPI == null)
			cDyn_MarkAPI = (MarkerAPI)cDyn_Plugin.getMarkerAPI();	
		
		if (cDyn_Markers == null)
			cDyn_Markers = cDyn_MarkAPI.getMarkerSet(
				this.getConfig().getString("markerset_name")
				);
		
		//Does this set exist?
		if (cDyn_Markers == null)
		{
			//Need to create a new icon set
			cDyn_Markers = cDyn_MarkAPI.createMarkerSet(
					this.getConfig().getString("markerset_name"),
					"DynmapNPCs",
					null,
					false);
		}
	
		cDyn_Markers.setHideByDefault(false);
		cDyn_Markers.setLabelShow(false);
		
		cDyn_Markers.setMinZoom(this.getConfig().getInt("zoomlevels.min"));
		cDyn_Markers.setMaxZoom(this.getConfig().getInt("zoomlevels.max"));

		cDyn_Markers.setMinZoom(0);
		cDyn_Markers.setMaxZoom(10);

	}
	
	private void SetupConfig()
	{
		this.saveDefaultConfig();
		this.reloadConfig();
	}
	
}
