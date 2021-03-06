package it.hijack.scheduler.data;

import it.hijack.scheduler.Worker;

import java.util.ArrayList;
import java.util.List;

public class WorkerProvider {
	
	private List<Worker> workers = new ArrayList<Worker>();

	public WorkerProvider() {
		workers.add(new Worker("Mario"));
		workers.add(new Worker("Luigi"));
		workers.add(new Worker("Giovanna"));
		workers.add(new Worker("Veronica"));
	}

	public List<Worker> getAll() {
		return workers;
	}
}
