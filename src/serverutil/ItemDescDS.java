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
}