package payne.framework.pigeon.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import payne.framework.pigeon.core.Constants;
import payne.framework.pigeon.core.Header;
import payne.framework.pigeon.core.Interceptor;
import payne.framework.pigeon.core.Invocation;
import payne.framework.pigeon.core.Path;
import payne.framework.pigeon.core.Pigeons;
import payne.framework.pigeon.core.Framework;
import payne.framework.pigeon.core.annotation.Accept;
import payne.framework.pigeon.core.annotation.Accept.Mode;
import payne.framework.pigeon.core.factory.bean.BeanFactory;
import payne.framework.pigeon.core.factory.stream.StreamFactory;
import payne.framework.pigeon.core.processing.Step;
import payne.framework.pigeon.core.protocol.Channel;
import payne.framework.pigeon.core.toolkit.CaseIgnoredList;

public class InvocationProcessor implements Interceptor, Constants {
	private final Path path;
	private final List<Accept.Mode> modes;
	private final List<String> media;
	private final Class<?> interfase;
	private final Method method;
	private final Object implementation;
	private final Set<Interceptor> interceptors;
	private final TreeMap<Class<? extends Annotation>, Step> processings;
	private final BeanFactory beanFactory;
	private final StreamFactory streamFactory;

	public InvocationProcessor(Path path, List<Mode> modes, List<String> media, Class<?> interfase, Method method, Object implementation, Set<Interceptor> interceptors, BeanFactory beanFactory, StreamFactory streamFactory) throws Exception {
		super();
		this.path = path;
		this.modes = new ArrayList<Accept.Mode>(modes != null ? modes : new ArrayList<Accept.Mode>());
		this.media = new CaseIgnoredList(media != null ? media : new ArrayList<String>());
		this.interfase = interfase;
		this.method = method;
		this.implementation = implementation;
		this.interceptors = new LinkedHashSet<Interceptor>(interceptors != null ? interceptors : new LinkedHashSet<Interceptor>());
		this.interceptors.add(this);
		this.beanFactory = beanFactory;
		this.streamFactory = streamFactory;
		this.processings = Pigeons.getMethodProcessings(method);
	}

	public void process(InvocationContext context, Channel channel) throws Exception {
		Invocation invocation = channel.read(path, method, beanFactory, streamFactory, new ArrayList<Step>(processings.values()));
		invocation.setProtocol(channel.getProtocol());
		invocation.setHost(channel.getHost());
		invocation.setPort(channel.getPort());
		invocation.setFile(channel.getFile());
		invocation.setMode(channel.getMode());
		invocation.setPath(path);
		invocation.setInterfase(interfase);
		invocation.setMethod(method);
		invocation.setImplementation(implementation);
		invocation.setInterceptors(interceptors.iterator());
		Header header = new Header();
		header.setCharset(context.getCharset());
		header.setDate(new Date());
		header.setServer(Framework.getCurrent().getName() + "/" + Framework.getCurrent().getVersion() + "(" + System.getProperty("os.name") + " " + System.getProperty("os.version") + ")");
		invocation.setServerHeader(header);
		Object result = invocation.invoke();
		invocation.setResult(result);
		invocation.setArguments(null);
		channel.write(path, invocation, beanFactory, streamFactory, new ArrayList<Step>(processings.values()));
	}

	public Object intercept(Invocation invocation) throws Exception {
		try {
			return method.invoke(implementation, invocation.getArguments());
		} catch (InvocationTargetException e) {
			throw e.getCause() != null && e.getCause() instanceof Exception ? (Exception) e.getCause() : e;
		}
	}

	public boolean accept(Mode mode) {
		return modes.contains(mode);
	}

	public boolean accept(String medium) {
		return media.isEmpty() || media.contains(medium);
	}

	public Path getPath() {
		return path;
	}

	public List<Accept.Mode> getModes() {
		return modes;
	}

	public List<String> getMedia() {
		return media;
	}

	public Class<?> getInterfase() {
		return interfase;
	}

	public Method getMethod() {
		return method;
	}

	public Object getImplementation() {
		return implementation;
	}

	public Set<Interceptor> getInterceptors() {
		return interceptors;
	}

	public TreeMap<Class<? extends Annotation>, Step> getProcessings() {
		return processings;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public StreamFactory getStreamFactory() {
		return streamFactory;
	}

}
