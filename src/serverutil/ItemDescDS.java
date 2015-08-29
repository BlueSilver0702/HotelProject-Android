package serverutil;

public class ItemDescDS {
	public Fraction units;
	public String item_name;
	public double price_per_unit;
	public double total_price;
	
	public ItemDescDS () {
		units = new Fraction();
		item_name = "";
		price_per_unit = 0.0;
		total_price = 0.0;
	}
	
	public ItemDescDS copy () {
		ItemDescDS retObj = new ItemDescDS();
		retObj.units = units.copy();
		retObj.item_name = item_name;
		retObj.price_per_unit = price_per_unit;
		retObj.total_price = total_price;
		return retObj;
	}
}