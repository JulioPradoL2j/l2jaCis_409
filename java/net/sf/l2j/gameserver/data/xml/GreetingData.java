package net.sf.l2j.gameserver.data.xml;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.commons.data.StatSet;
import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.gameserver.model.holder.GreetingHolder;
import net.sf.l2j.gameserver.taskmanager.GreetingManager;

import org.w3c.dom.Document;

public class GreetingData implements IXmlReader
{
	private final Map<Integer, GreetingHolder> _greetings = new HashMap<>();
	
	public GreetingData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/xml/greeting.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _greetings.size() + " greetings.");
		GreetingManager.start();
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> {
			forEach(listNode, "greeting", greetNode -> {
				StatSet set = new StatSet(parseAttributes(greetNode));
				GreetingHolder holder = new GreetingHolder(set);
				_greetings.put(holder.getCastleId(), holder);
			});
		});
	}
	
	public GreetingHolder getGreeting(int castleId)
	{
		return _greetings.get(castleId);
	}
	
	public static GreetingData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final GreetingData _instance = new GreetingData();
	}
}
