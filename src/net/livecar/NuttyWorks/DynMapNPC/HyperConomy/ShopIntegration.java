package net.livecar.NuttyWorks.DynMapNPC.HyperConomy;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

import grokswell.hypermerchant.HyperMerchantTrait;
import regalowl.hyperconomy.HyperConomy;
import regalowl.hyperconomy.bukkit.BukkitConnector;
import regalowl.hyperconomy.shop.Shop;
import regalowl.hyperconomy.tradeobject.EnchantmentClass;
import regalowl.hyperconomy.tradeobject.TradeObject;
import regalowl.hyperconomy.tradeobject.TradeObjectStatus;
import regalowl.hyperconomy.tradeobject.TradeObjectType;
import net.citizensnpcs.api.npc.NPC;
import net.livecar.NuttyWorks.DynMapNPC.DynMapNPC;

public class ShopIntegration {
    private final DynMapNPC plugin;
    
    public ShopIntegration(DynMapNPC plugin) {
        this.plugin = plugin;
    }
	
    public String GetShop(NPC oNPC)
    {
    	if (!oNPC.hasTrait(HyperMerchantTrait.class)) {
    		return "";
    	} 
    	HyperMerchantTrait trait = oNPC.getTrait(HyperMerchantTrait.class);
    	String ShopName = trait.trait_key.getString("shop_name");
    	BukkitConnector oHyperBukkit = (BukkitConnector)plugin.getServer().getPluginManager().getPlugin("HyperConomy");
    	HyperConomy oHyperPlug = (HyperConomy)oHyperBukkit.getHC();	
    	Shop oShop = oHyperPlug.getHyperShopManager().getShop(ShopName);
    	String sResult = "";
    	
        ArrayList<TradeObject> objects = oShop.getTradeableObjects();
        Collections.sort(objects);
        Boolean bAlternateColor = true;
        
        for (TradeObject ho : objects)
        {
        	TradeObjectStatus hos = null;
        	if (ho.isShopObject())
        	{
        		hos = ho.getShopObjectStatus();
        		if (hos == TradeObjectStatus.NONE) {}
        	}
        	else
        	{
        		if (!oHyperPlug.enabled()) {
        			return "";
        		}
        		
	            double sellPrice = -1.0D;
	            double buyPrice = -1.0D;
	            String buyString = "";
	            String sellString = "";

	            if (ho.getType() == TradeObjectType.ITEM)
	            {
	            	sellPrice = ho.getSellPrice(1.0D);
	            	sellPrice -= ho.getSalesTaxEstimate(sellPrice);
	            	buyPrice = ho.getBuyPrice(1.0D);
	            	buyPrice += ho.getPurchaseTax(buyPrice);
	                buyString = oHyperPlug.getLanguageFile().fC(Math.round(buyPrice * 100)/100);
	                sellString = oHyperPlug.getLanguageFile().fC(Math.round(sellPrice * 100)/100);
	            }
	            else if (ho.getType() == TradeObjectType.ENCHANTMENT)
	            {
	            	sellPrice = ho.getSellPrice(EnchantmentClass.DIAMOND);
	            	sellPrice -= ho.getSalesTaxEstimate(sellPrice);
	            	buyPrice = ho.getBuyPrice(EnchantmentClass.DIAMOND);
	            	buyPrice += ho.getPurchaseTax(buyPrice);
	                buyString = oHyperPlug.getLanguageFile().fC(Math.round(buyPrice * 100)/100);
	                sellString = oHyperPlug.getLanguageFile().fC(Math.round(sellPrice * 100)/100);
	            }
	            else if (ho.getType() == TradeObjectType.EXPERIENCE)
	            {
	            	sellPrice = ho.getSellPrice(1.0D);
	            	sellPrice -= ho.getSalesTaxEstimate(sellPrice);
	            	buyPrice = ho.getBuyPrice(1.0D);
	            	buyPrice += ho.getPurchaseTax(buyPrice);
	                buyString = oHyperPlug.getLanguageFile().fC(Math.round(buyPrice * 100)/100);
	                sellString = oHyperPlug.getLanguageFile().fC(Math.round(sellPrice * 100)/100);
	            }
	            String sRow = plugin.getConfig().getString((bAlternateColor?"hyperconomy.row":"hyperconomy.altrow"));
	            sRow = sRow.replace("%displayname%",ho.getDisplayName().replace('_', ' '));
	            sRow = sRow.replace("%sellamount%",sellString);
	            sRow = sRow.replace("%buyamount%",buyString);
	            sRow = sRow.replace("%stock%",new DecimalFormat("#,##0").format(ho.getStock()));
	            bAlternateColor = !bAlternateColor;
	            sResult += sRow + "\n";
        	}
        }
    	return plugin.getConfig().getString("hyperconomy.table").replace("%rows%",sResult);
    }
    
}
