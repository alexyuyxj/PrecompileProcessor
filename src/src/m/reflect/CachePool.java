package m.reflect;

/**
 * 一个简易的LRU缓存池。<p>
 * 
 * 构造实例时需要制定缓存池容量，缓存数据超过制定容量时，会删除队列尾部数据。<br>
 * 新数据默认会插在队列头部。每次读取数据时，被读取的项目也会重新被提到队列头部。<br>
 * 不接受空key。<br>
 * get和put都是同步方法。
 */
public class CachePool<K, V> {
	private int capacity;
	private int size;
	private Node<K, V> head;
	private Node<K, V> tail;
	
	public CachePool(int capacity) {
		this.capacity = capacity;
	}
	
	public synchronized boolean put(K key, V value) {
		if (key == null || capacity <= 0) {
			return false;
		}
		
		Node<K, V> n = null;
		while (size >= capacity) {
			n = tail;
			if (n == null) { // 这肯定是出错了，重新寻找队尾
				Node<K, V> n1 = head;
				if (n1 == null) {
					size = 0;
					tail = null;
				} else {
					size = 1;
					while (n1.next != null) {
						size++;
						n1 = n1.next;
					}
					tail = n1;
				}
			} else {
				tail = tail.previous;
				tail.next = null;
				size--;
			}
		}
		
		if (n == null) {
			n = new Node<K, V>();
		}
		n.cacheTime = System.currentTimeMillis();
		n.key = key;
		n.value = value;
		n.previous = null;
		n.next = head;
		
		if (size == 0) {
			tail = n;
		} else if (head != null) {
			head.previous = n;
		} else { // 这肯定是出错了，要做数据清理
			tail = n;
			size = 0;
		}
		head = n;
		size++;
		return true;
	}
	
	public synchronized V get(K key) {
		if (head == null) {
			size = 0;
			tail = null;
			return null;
		} else if (head.key.equals(key)) {
			return head.value;
		}
		
		Node<K, V> n = head;
		while (n.next != null) {
			n = n.next;
			if (n.key.equals(key)) {
				if (n.next == null) {
					n.previous.next = null;
					tail = n.previous;
				} else {
					n.previous.next = n.next;
					n.next.previous = n.previous;
				}
				
				n.previous = null;
				n.next = head;
				head.previous = n;
				head = n;
				return n.value;
			}
		}
		
		return null;
	}
	
	public synchronized void clear() {
		head = tail = null;
		size = 0;
	}
	
	public synchronized void trimBeforeTime(long time) {
		if (capacity <= 0) {
			return;
		}
		
		Node<K, V> n = head;
		while (n != null) {
			if (n.cacheTime < time) {
				if (n.previous != null) {
					n.previous.next = n.next;
				}
				if (n.next != null) {
					n.next.previous = n.previous;
				}
				if (n.equals(head)) {
					head = head.next;
				}
				size--;
			}
			n = n.next;
		}
	}
	
	public int size() {
		return size;
	}
	
	private static class Node<K, V> {
		public K key;
		public V value;
		public Node<K, V> previous;
		public Node<K, V> next;
		private long cacheTime;
	}
	
}
