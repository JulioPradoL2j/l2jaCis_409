package net.sf.l2j.gameserver.data.manager;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.data.xml.PartyFarmData;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.holder.PTFarmConfig;
import net.sf.l2j.gameserver.model.holder.PTFarmHolder;
import net.sf.l2j.gameserver.model.spawn.Spawn;

public class PartyFarmEvent
{
	public static Logger LOGGER = Logger.getLogger(PartyFarmEvent.class.getName());
	private static ScheduledFuture<?> eventChecker;
	private static boolean isRunning;
	private static List<Spawn> activeSpawns = Collections.synchronizedList(new ArrayList<>());
	private static String lastEventTime;
	
	public static void start()
	{
		if (eventChecker == null || eventChecker.isCancelled())
			eventChecker = ThreadPool.scheduleAtFixedRate(() -> checkAndStartEvent(), 500, 1000);
	}
	
	private static void checkAndStartEvent()
	{
		LocalDateTime now = LocalDateTime.now();
		int currentDay = now.getDayOfWeek().getValue() % 7; // 0 = domingo
		
		PTFarmConfig config = PartyFarmData.getInstance().getConfig();
		
		if (!config.isEnabled() || !config.getDays().contains(currentDay))
			return;
		
		String nowStr = new SimpleDateFormat("HH:mm").format(new Date());
		
		for (String time : config.getTimes())
		{
			if (nowStr.equals(time) && !isRunning && !nowStr.equals(lastEventTime))
			{
				isRunning = true;
				lastEventTime = nowStr;
				World.announceToOnlinePlayers("The Party Farm will start in " + config.getPreparation() + " minutes!", true);
				ThreadPool.schedule(() -> spawnMobs(), 1000 * 60 * config.getPreparation());
				unSpwan();
				break;
			}
		}
	}
	
	private static void spawnMobs()
	{
		World.announceToOnlinePlayers("The Party Farm has started! Good drops!", true);
		
		List<PTFarmHolder> spawns = PartyFarmData.getInstance().getSpawns("partyfarm");
		PTFarmConfig config = PartyFarmData.getInstance().getConfig();
		
		for (PTFarmHolder holder : spawns)
		{
			for (int i = 0; i < holder.getCount(); i++)
			{
				try
				{
					final NpcTemplate template = NpcData.getInstance().getTemplate(holder.getNpcId());
					if (template == null)
					{
						LOGGER.info("[PartyFarmEvent] Template not found for npcId: " + holder.getNpcId());
						continue;
					}
					
					Spawn spawn = new Spawn(template);
					
					int x = holder.getX();
					int y = holder.getY();
					int z = holder.getZ();
					
					if (holder.getCount() > 1)
					{
						int radius = 400;
						double angle = Math.random() * 2 * Math.PI;
						int randX = (int) (Math.cos(angle) * (Math.random() * radius));
						int randY = (int) (Math.sin(angle) * (Math.random() * radius));
						x += randX;
						y += randY;
					}
					
					spawn.setLoc(x, y, z, 0);
				
					
					spawn.doSpawn(false);
	
					activeSpawns.add(spawn);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		World.announceToOnlinePlayers("The Party Farm event ends in " + config.getDuration() + " minutes!", true);
		ThreadPool.schedule(() -> endEvent(), 1000 * 60 * config.getDuration());
	}
	
	private static void endEvent()
	{
		World.announceToOnlinePlayers("The Party Farm event has ended!", false);
		unSpwan();
		activeSpawns.clear();
		isRunning = false;
		lastEventTime = "";
	}
	
	private static void unSpwan()
	{
		for (Spawn spawn : activeSpawns)
		{
			if (spawn != null)
				spawn.doDelete();
			

		}
	}
	
	public static boolean isRunning()
	{
		return isRunning;
	}
	
	public static String lastEvent()
	{
		return lastEventTime;
	}
	
	public static void reset()
	{
		if (eventChecker != null)
		{
			eventChecker.cancel(false);
			eventChecker = null;
		}
	}
}
