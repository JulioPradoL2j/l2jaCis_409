package net.sf.l2j.gameserver.taskmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.data.xml.GreetingData;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.ZoneType;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.GreetingHolder;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.residence.castle.Castle;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage.SMPOS;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;

public class GreetingManager
{
	private static final int RADIUS = 500; // Raio de proximidade
	private static final Map<Integer, Long> _greetCooldown = new ConcurrentHashMap<>();
	private static ScheduledFuture<?> _greetingChecker;
	
	public static void start()
	{
		if (_greetingChecker == null || _greetingChecker.isCancelled())
		{
			_greetingChecker = ThreadPool.scheduleAtFixedRate(() -> checkGreetings(), 5000, 5000);
		}
	}
	
	private static void checkGreetings()
	{
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			GreetingHolder holder = GreetingData.getInstance().getGreeting(castle.getId());
			if (holder == null || holder.getZoneType() != ZoneType.TOWN)
				continue;
			
			int ownerId = castle.getOwnerId();
			if (ownerId <= 0)
				continue;
			
			Clan clan = ClanTable.getInstance().getClan(ownerId);
			if (clan == null || clan.getLeader() == null)
				continue;
			
			Player leader = clan.getLeader().getPlayerInstance();
			if (leader == null || !leader.isInsideZone(ZoneId.TOWN))
				continue;
			
			long lastTime = _greetCooldown.getOrDefault(castle.getId(), 0L);
			if (System.currentTimeMillis() - lastTime < (holder.getTime() * 1000L))
				continue;
			
			for (Player player : World.getInstance().getPlayers())
			{
				if (player == null || player.isDead() || !player.isInsideZone(ZoneId.TOWN))
					continue;
				
				if (player == leader || !MathUtil.checkIfInRange(RADIUS, leader, player, true))
					continue;
				
				if (player.isOperating() || player.getActiveRequester() != null || player.isAlikeDead() || player.getAI().getCurrentIntention().getType() != IntentionType.IDLE)
					continue;
				
				player.broadcastPacket(new SocialAction(player, holder.getActionUse()));
				
				player.sendMessage(holder.getMessage());
				
				player.sendPacket(new ExShowScreenMessage(holder.getMessage(), 1100, SMPOS.TOP_CENTER, false));
			}
			
			_greetCooldown.put(castle.getId(), System.currentTimeMillis());
		}
	}
	
}
