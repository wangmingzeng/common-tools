
package cn.com.zach.tools.command;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * jdk中只有ConcurrentHashMap没有ConcurrentHashSet. 在很多线程环境下需要ConcurrentHashSet, 参照HashSet使用ConcurrentHashMap实现ConcurrentHashSet
 *
 * @author huangchao
 *
 * @param <E>
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E>, java.io.Serializable
{

	private static final long					serialVersionUID	= -8672117787651310382L;

	private static final Object					PRESENT				= new Object();

	//使用ConcurrentHashMap存储键值对
	private final ConcurrentHashMap<E, Object>	map;

	public ConcurrentHashSet()
	{
		map = new ConcurrentHashMap<E, Object>();
	}

	public ConcurrentHashSet(int initialCapacity)
	{
		map = new ConcurrentHashMap<E, Object>( initialCapacity );
	}

	@Override
	public Iterator<E> iterator()
	{
		return map.keySet().iterator();
	}

	@Override
	public int size()
	{
		return map.size();
	}

	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return map.containsKey( o );
	}

	@Override
	public boolean add(E e)
	{
		return map.put( e ,PRESENT ) == null;
	}

	@Override
	public boolean remove(Object o)
	{
		return map.remove( o ) == PRESENT;
	}

	@Override
	public void clear()
	{
		map.clear();
	}
}
