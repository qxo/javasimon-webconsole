package org.javasimon.console.action;

import java.io.IOException;
import java.util.Iterator;
import javax.servlet.ServletException;
import org.javasimon.Sample;
import org.javasimon.Simon;
import org.javasimon.SimonState;
import org.javasimon.console.*;
import org.javasimon.console.json.JsonStringifierFactory;
import org.javasimon.console.json.ObjectJS;
import org.javasimon.console.json.SimpleJS;
import org.javasimon.console.text.Stringifier;

/**
 * Base for actions returing data in JSON format
 *
 * @author gquintana
 */
public abstract class AbstractJsonAction extends Action {

	protected AbstractJsonAction(ActionContext context) {
		super(context);
	}
	/**
	 * Type converter Object &rarr; JSON String
	 */
	protected final JsonStringifierFactory jsonStringifierFactory=new JsonStringifierFactory();
	/**
	 * Flag indicating  if simons should be reset during sampling
	 */
	protected boolean reset;
	
	@Override
	public void readParameters() {
		TimeFormatType timeFormat=getContext().getParameterAsEnum("timeFormat", TimeFormatType.class, TimeFormatType.MILLISECOND);
		jsonStringifierFactory.init(timeFormat, 
			JsonStringifierFactory.READABLE_DATE_PATTERN,// Should be ISO
			JsonStringifierFactory.INTEGER_NUMBER_PATTERN
			);
		reset=getContext().getParameterAsBoolean("reset", Boolean.FALSE);
	}
	private <T> void addAttribute(ObjectJS objectJS, String name, Class<T> type, T value) {
		objectJS.setAttribute(name, new SimpleJS<T>(value,jsonStringifierFactory.getStringifier(type)));
	}
	/**
	 * Transforms a Simon into a JSON object
	 *
	 * @param simon Simon
	 * @return JSON object
	 */
	protected ObjectJS createObjectJS(Simon simon) {
		Sample sample=reset?simon.sampleAndReset():simon.sample();
		SimonType lType = SimonTypeFactory.getValueFromInstance(sample);
		ObjectJS objectJS = ObjectJS.create(sample, jsonStringifierFactory);
		addAttribute(objectJS, "type",		SimonType.class,	lType);
		addAttribute(objectJS, "enabled",	Boolean.class,		simon.isEnabled());
		addAttribute(objectJS, "state",		SimonState.class,	simon.getState());
		return objectJS;

	}
	@SuppressWarnings("unchecked")
	protected final ObjectJS createAttributesObjectJS(Simon simon) {
		ObjectJS objectJS = new ObjectJS();
		Iterator<String> attributeNameIter=simon.getAttributeNames();
		boolean foundAttribute=false;
		while(attributeNameIter.hasNext()) {
			String attributeName=attributeNameIter.next();
			Object attributeValue=simon.getAttribute(attributeName);
			if (attributeValue!=null) {
				Stringifier attributeStringifier=jsonStringifierFactory
					.getStringifier(attributeValue.getClass());
				if (attributeStringifier!=null) {
					objectJS.setSimpleAttribute(attributeName, attributeValue, attributeStringifier);
					foundAttribute=true;
				}
			}

		}
		return foundAttribute?objectJS:null;
	}

	@Override
	public void execute() throws ServletException, IOException, ActionException {
		dontCache();
		getContext().setContentType("application/json");
	}
}
