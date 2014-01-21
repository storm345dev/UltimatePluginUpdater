package net.stormdev.bukkitmods.ultimatepluginupdater.main;

public class ChangingBoolean {
	private Boolean boon = true;
    public ChangingBoolean(Boolean boon){
        this.boon = boon;
    }
    public Boolean getValue(){
    	return this.boon;
    }
    public void setValue(Boolean val){
    	this.boon = val;
    }
}
