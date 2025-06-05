package net.sf.l2j.gameserver.model.holder;

import net.sf.l2j.commons.data.StatSet;
import net.sf.l2j.gameserver.enums.ZoneType;


public class GreetingHolder
{
	private final int _castleId;
	private final ZoneType _zoneType;
	private final int _actionUse;
	private final String _mgs;
	private final int _time;
	
	public GreetingHolder(StatSet set)
	{
		_castleId = set.getInteger("castleId");
		_zoneType = Enum.valueOf(ZoneType.class, set.getString("zone"));
		_actionUse = set.getInteger("actionId", 7);
		_mgs = set.getString("msg", "");
		_time = set.getInteger("time", 30);
	}
	
	public int getCastleId()
	{
		return _castleId;
	}
	
	public ZoneType getZoneType()
	{
		return _zoneType;
	}
	
	public int getActionUse()
	{
		return _actionUse;
	}
	
	public String getMessage()
	{
		return _mgs;
	}
	
	public int getTime()
	{
		return _time;
	}
}
