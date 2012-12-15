package com.github.catageek.ByteCart;

import org.bukkit.scheduler.BukkitTask;

public final class DelayedThreadManager<K> {

	final private class IdRunnable {
		public final int id;
		public final Runnable task;
		
		public IdRunnable(int id, Runnable task) {
			this.id = id;
			this.task = task;
		}
	}
	
	final private BlockMap<K, IdRunnable> DelayedThread = new BlockMap<K, IdRunnable>();

	/*
	 * create a release task
	 */
	private synchronized final void createReleaseTask(K block, int duration, Runnable ReleaseTask) {
		
		Runnable task = new Execute(this, ReleaseTask, block);
		int id = ByteCart.myPlugin.getServer().getScheduler().scheduleSyncDelayedTask(ByteCart.myPlugin, task
				, duration);

		// the id of the thread and the task are stored in a static map
		this.DelayedThread.createEntry(block, new IdRunnable(id, task));
	}

	private synchronized final void createUnsynchronizedReleaseTask(K block, int duration, Runnable ReleaseTask) {
		Runnable task = new Execute(this, ReleaseTask, block);
		BukkitTask bt = ByteCart.myPlugin.getServer().getScheduler().runTaskLaterAsynchronously(ByteCart.myPlugin, task, duration);

		// the id of the thread is stored in a static map
		this.DelayedThread.createEntry(block, new IdRunnable(bt.getTaskId(), task));
	}


	protected final boolean hasReleaseTask(K block) {
		return this.DelayedThread.hasEntry(block);
	}


	/*
	 * Renew the timer of release task
	 */
	protected synchronized final void renew(K block, int duration, Runnable ReleaseTask) {
		if(this.hasReleaseTask(block)) {
			// we cancel the release task
			ByteCart.myPlugin.getServer().getScheduler().cancelTask((Integer) this.DelayedThread.getValue(block).id);
			// we schedule a new one
			int id = ByteCart.myPlugin.getServer().getScheduler().scheduleSyncDelayedTask(ByteCart.myPlugin, this.DelayedThread.getValue(block).task, duration);
			// we update the hashmap
			this.DelayedThread.updateValue(block, new IdRunnable(id, this.DelayedThread.getValue(block).task));
		}
		else {
			this.createReleaseTask(block, duration, ReleaseTask);
		}
	}

	protected synchronized final void renewAsync(K block, int duration, Runnable ReleaseTask) {
		if(this.hasReleaseTask(block)) {
			// we cancel the release task
			ByteCart.myPlugin.getServer().getScheduler().cancelTask((Integer) this.DelayedThread.getValue(block).id);
			// we schedule a new one
			//int id = ByteCart.myPlugin.getServer().getScheduler().scheduleAsyncDelayedTask(ByteCart.myPlugin, new Execute(this, ReleaseTask, block), duration);
			BukkitTask bt = ByteCart.myPlugin.getServer().getScheduler().runTaskLaterAsynchronously(ByteCart.myPlugin, this.DelayedThread.getValue(block).task, duration);
			// we update the hashmap
			this.DelayedThread.updateValue(block, new IdRunnable(bt.getTaskId(), this.DelayedThread.getValue(block).task));
		}
		else {
			this.createUnsynchronizedReleaseTask(block, duration, ReleaseTask);
		}
	}

	private final void free(K block) {
		this.DelayedThread.deleteEntry(block);
	}

	private final class Execute implements Runnable {

		private final Runnable task;
		private final DelayedThreadManager<K> dtm;
		private final K block;

		public Execute(DelayedThreadManager<K> dtm, Runnable task, K block) {
			this.task = task;
			this.dtm = dtm;
			this.block = block;
		}

		@Override
		public void run() {
			this.dtm.free(block);			
			this.task.run();
		}

	}

}