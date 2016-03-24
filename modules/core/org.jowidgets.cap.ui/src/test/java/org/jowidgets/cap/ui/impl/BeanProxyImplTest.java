/*
 * Copyright (c) 2016, grossmann
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * * Neither the name of the jo-widgets.org nor the
 *   names of its contributors may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL jo-widgets.org BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package org.jowidgets.cap.ui.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.IControl;
import org.jowidgets.api.widgets.IFrame;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.api.widgets.IValidationResultLabel;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanDtoBuilder;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.common.tools.bean.BeanDtoBuilder;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeSet;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.form.IBeanFormControlFactory;
import org.jowidgets.cap.ui.api.form.IBeanFormLayouter;
import org.jowidgets.cap.ui.api.widgets.IBeanForm;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.impl.beans.Gender;
import org.jowidgets.cap.ui.impl.beans.IPerson;
import org.jowidgets.cap.ui.impl.beans.PersonDtoDescriptorBuilder;
import org.jowidgets.cap.ui.impl.beans.PersonNameValidator;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.factory.ICustomWidgetFactory;
import org.jowidgets.test.tools.TestLoggerProvider;
import org.jowidgets.test.tools.TestToolkit;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.ValueHolder;
import org.jowidgets.validation.IValidationMessage;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.MessageType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BeanProxyImplTest {

	private static final long PERSON_ID = 1l;
	private static final String PERSON_NAME = "Hans";
	private static final String PERSON_LAST_NAME = "Maier";
	private static final Gender PERSON_GENDER = Gender.MALE;

	private static final long INVALID_PERSON_ID = 2l;
	private static final String INVALID_PERSON_NAME = PersonNameValidator.FORBIDDEN_NAME;
	private static final String INVALID_PERSON_LAST_NAME = "Estrada";
	private static final Gender INVALID_PERSON_GENDER = Gender.MALE;

	private IBeanDtoDescriptor personDescriptor;
	private IAttributeSet personAttributes;
	private List<IBeanPropertyValidator<IPerson>> personValidators;

	private IBeanDto personDto;
	private IBeanDto invalidPersonDto;

	@Before
	public void setUp() {
		TestLoggerProvider.setUpLoggingBeforeTest();
		TestToolkit.setUpToolkitBeforeTest();
		this.personDescriptor = new PersonDtoDescriptorBuilder().build();
		this.personAttributes = createAttributes(personDescriptor);
		this.personValidators = createValidators(personDescriptor, personAttributes.getAttributes());

		this.personDto = createPersonDto(PERSON_ID, PERSON_NAME, PERSON_LAST_NAME, PERSON_GENDER);

		this.invalidPersonDto = createPersonDto(
				INVALID_PERSON_ID,
				INVALID_PERSON_NAME,
				INVALID_PERSON_LAST_NAME,
				INVALID_PERSON_GENDER);
	}

	@After
	public void tearDown() {
		TestToolkit.tearDownToolkitAfterTest();
		TestLoggerProvider.tearDownLoggingAfterTest();
	}

	private IBeanProxy<IPerson> createPersonProxy(final IBeanDto beanDto) {
		final List<IBeanPropertyValidator<IPerson>> emptyValidatory = Collections.emptyList();
		return createPersonProxy(beanDto, emptyValidatory);
	}

	private IBeanProxy<IPerson> createPersonProxy(final IBeanDto beanDto, final List<IBeanPropertyValidator<IPerson>> validators) {
		final IBeanProxyFactory<IPerson> factory = CapUiToolkit.beanProxyFactory(IPerson.class, IPerson.class);
		final IBeanProxy<IPerson> result = factory.createProxy(beanDto, personAttributes);
		result.addBeanPropertyValidators(validators);
		return result;
	}

	private static IBeanDto createPersonDto(final long id, final String name, final String lastName, final Gender gender) {
		final IBeanDtoBuilder builder = new BeanDtoBuilder(IPerson.class);
		builder.setId(Long.valueOf(id));
		builder.setValue(IPerson.NAME_PROPERTY, name);
		builder.setValue(IPerson.LAST_NAME_PROPERTY, lastName);
		builder.setValue(IPerson.GENDER_PROPERTY, gender);
		return builder.build();
	}

	private static IAttributeSet createAttributes(final IBeanDtoDescriptor decsriptor) {
		final IAttributeToolkit attributeToolkit = CapUiToolkit.attributeToolkit();
		return attributeToolkit.attributeSet(attributeToolkit.createAttributes(decsriptor.getProperties()));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static <BEAN_TYPE> List<IBeanPropertyValidator<BEAN_TYPE>> createValidators(
		final IBeanDtoDescriptor descriptor,
		final Collection<IAttribute<Object>> attributes) {
		final List<IBeanPropertyValidator<BEAN_TYPE>> result = new LinkedList<IBeanPropertyValidator<BEAN_TYPE>>();

		result.add(new BeanPropertyValidatorImpl<BEAN_TYPE>(attributes));
		for (final IBeanValidator beanValidator : descriptor.getValidators()) {
			result.add(new BeanPropertyValidatorAdapter<BEAN_TYPE>(beanValidator));
		}
		return result;
	}

	@Test
	public void testValidBeanProxyCreation() {
		final IBeanProxy<IPerson> personProxy = createPersonProxy(personDto, personValidators);
		final IPerson person = personProxy.getBean();
		Assert.assertEquals(PERSON_NAME, person.getName());
		Assert.assertEquals(PERSON_LAST_NAME, person.getLastname());
		Assert.assertEquals(PERSON_GENDER, person.getGender());

		Assert.assertFalse(personProxy.isModified(IPerson.NAME_PROPERTY));
		Assert.assertFalse(personProxy.isModified(IPerson.LAST_NAME_PROPERTY));
		Assert.assertFalse(personProxy.isModified(IPerson.GENDER_PROPERTY));

		final IValidationResult validationResult = personProxy.validate();
		Assert.assertTrue(validationResult.isOk());
	}

	@Test
	public void testInvalidBeanProxyCreation() {
		final IBeanProxy<IPerson> invalidPersonProxy = createPersonProxy(invalidPersonDto, personValidators);
		final IPerson invalidPerson = invalidPersonProxy.getBean();
		Assert.assertEquals(INVALID_PERSON_NAME, invalidPerson.getName());
		Assert.assertEquals(INVALID_PERSON_LAST_NAME, invalidPerson.getLastname());
		Assert.assertEquals(INVALID_PERSON_GENDER, invalidPerson.getGender());

		Assert.assertFalse(invalidPersonProxy.isModified(IPerson.NAME_PROPERTY));
		Assert.assertFalse(invalidPersonProxy.isModified(IPerson.LAST_NAME_PROPERTY));
		Assert.assertFalse(invalidPersonProxy.isModified(IPerson.GENDER_PROPERTY));

		final IValidationResult validationResult = invalidPersonProxy.validate();
		Assert.assertFalse(validationResult.isValid());
		final IValidationMessage worstFirst = validationResult.getWorstFirst();
		Assert.assertEquals(personAttributes.getAttribute(IPerson.NAME_PROPERTY).getCurrentLabel(), worstFirst.getContext());
		Assert.assertEquals(MessageType.ERROR, worstFirst.getType());
	}

	@Test
	public void testValidateInvalidPersonAfterValidatorsAdded() {
		final IBeanProxy<IPerson> invalidPersonProxy = createPersonProxy(invalidPersonDto);

		IValidationResult validationResult = invalidPersonProxy.validate();
		Assert.assertTrue(validationResult.isOk());

		//after adding validators, bean must have validation error
		invalidPersonProxy.addBeanPropertyValidators(personValidators);

		validationResult = invalidPersonProxy.validate();
		Assert.assertFalse(validationResult.isValid());
		final IValidationMessage worstFirst = validationResult.getWorstFirst();
		Assert.assertEquals(personAttributes.getAttribute(IPerson.NAME_PROPERTY).getCurrentLabel(), worstFirst.getContext());
		Assert.assertEquals(MessageType.ERROR, worstFirst.getType());
	}

	@Test
	public void testAllValidators() {
		testAllValidatorsForInitialValidPerson(createPersonProxy(personDto, personValidators));
	}

	@Test
	public void testAllValidatorsWithExternalValidatorBeanForm() {
		final IBeanProxy<IPerson> personProxy = createPersonProxy(personDto, personValidators);

		final IBeanForm<IPerson> beanForm = createBeanForm();
		beanForm.setValue(personProxy);

		testAllValidatorsForInitialValidPerson(personProxy);
	}

	@Test
	public void testValidationResultWithExternalValidatorBeanForm() {
		final IBeanProxy<IPerson> personProxy = createPersonProxy(personDto, personValidators);

		final ValueHolder<IInputControl<String>> nameControl = new ValueHolder<IInputControl<String>>();
		final ValueHolder<IInputControl<String>> lastnameControl = new ValueHolder<IInputControl<String>>();

		final ValueHolder<IValidationResultLabel> nameValidationLabel = new ValueHolder<IValidationResultLabel>();
		final ValueHolder<IValidationResultLabel> lastnameValidationlabel = new ValueHolder<IValidationResultLabel>();

		final IBeanForm<IPerson> beanForm = createPersonBeanForm(
				nameControl,
				lastnameControl,
				nameValidationLabel,
				lastnameValidationlabel);

		beanForm.setValue(personProxy);
		Assert.assertTrue(personProxy.validate().isOk());

		//make name invalid
		nameControl.get().setValue(PersonNameValidator.FORBIDDEN_NAME);
		Assert.assertEquals(MessageType.ERROR, personProxy.validate().getWorstFirst().getType());
		Assert.assertEquals(MessageType.ERROR, nameValidationLabel.get().getResult().getWorstFirst().getType());
		Assert.assertNull(lastnameValidationlabel.get().getResult());

		//modify person lastname must not remove name error
		lastnameControl.get().setValue("Foo");
		Assert.assertEquals(MessageType.ERROR, personProxy.validate().getWorstFirst().getType());

		//make lastname invalid
		lastnameControl.get().setValue(null);
		Assert.assertEquals(MessageType.ERROR, personProxy.validate().getWorstFirst().getType());
		Assert.assertEquals(MessageType.ERROR, nameValidationLabel.get().getResult().getWorstFirst().getType());
		Assert.assertEquals(MessageType.INFO_ERROR, lastnameValidationlabel.get().getResult().getWorstFirst().getType());

		//make name valid
		nameControl.get().setValue("Foo");
		Assert.assertEquals(MessageType.INFO_ERROR, personProxy.validate().getWorstFirst().getType());
		Assert.assertEquals(MessageType.OK, nameValidationLabel.get().getResult().getWorstFirst().getType());
		Assert.assertEquals(MessageType.INFO_ERROR, lastnameValidationlabel.get().getResult().getWorstFirst().getType());

		//make all valid again
		personProxy.undoModifications();
		Assert.assertTrue(personProxy.validate().isOk());
	}

	private IBeanForm<IPerson> createBeanForm() {
		return createPersonBeanForm(
				new ValueHolder<IInputControl<String>>(),
				new ValueHolder<IInputControl<String>>(),
				new ValueHolder<IValidationResultLabel>(),
				new ValueHolder<IValidationResultLabel>());
	}

	private IBeanForm<IPerson> createPersonBeanForm(
		final ValueHolder<IInputControl<String>> nameControl,
		final ValueHolder<IInputControl<String>> lastNameControl,
		final ValueHolder<IValidationResultLabel> nameValidationlabel,
		final ValueHolder<IValidationResultLabel> lastnameValidationlabel) {

		final IFrame rootFrame = Toolkit.createRootFrame(BPF.frame());
		final IBeanFormBluePrint<IPerson> beanFormBp = CapUiToolkit.bluePrintFactory().beanForm(
				IPerson.class,
				personAttributes.getAttributes());

		beanFormBp.setEditModeLayouter(createPersonBeanFormLayouter(
				nameControl,
				lastNameControl,
				nameValidationlabel,
				lastnameValidationlabel));

		final IBeanForm<IPerson> result = rootFrame.add(beanFormBp);
		rootFrame.setVisible(true);
		return result;
	}

	private IBeanFormLayouter createPersonBeanFormLayouter(
		final ValueHolder<IInputControl<String>> nameControl,
		final ValueHolder<IInputControl<String>> lastNameControl,
		final ValueHolder<IValidationResultLabel> nameValidationlabel,
		final ValueHolder<IValidationResultLabel> lastnameValidationlabel) {
		return new IBeanFormLayouter() {
			@Override
			public void layout(final IContainer container, final IBeanFormControlFactory controlFactory) {
				addInputControlString(container, controlFactory, IPerson.NAME_PROPERTY, nameControl);
				addValidationLabel(container, controlFactory, IPerson.NAME_PROPERTY, nameValidationlabel);

				addInputControlString(container, controlFactory, IPerson.LAST_NAME_PROPERTY, lastNameControl);
				addValidationLabel(container, controlFactory, IPerson.LAST_NAME_PROPERTY, lastnameValidationlabel);

				container.add(controlFactory.createControl(IPerson.GENDER_PROPERTY));
				container.add(controlFactory.createPropertyValidationLabel(IPerson.GENDER_PROPERTY));
			}
		};
	}

	private static void addInputControlString(
		final IContainer container,
		final IBeanFormControlFactory controlFactory,
		final String propertyName,
		final ValueHolder<IInputControl<String>> valueHolder) {
		container.add(new ICustomWidgetCreator<IControl>() {
			@SuppressWarnings("unchecked")
			@Override
			public IControl create(final ICustomWidgetFactory widgetFactory) {
				final ICustomWidgetCreator<? extends IControl> original = controlFactory.createControl(propertyName);
				final IControl result = original.create(widgetFactory);
				valueHolder.set((IInputControl<String>) result);
				return result;
			}
		});
	}

	private static void addValidationLabel(
		final IContainer container,
		final IBeanFormControlFactory controlFactory,
		final String propertyName,
		final ValueHolder<IValidationResultLabel> valueHolder) {
		container.add(new ICustomWidgetCreator<IControl>() {
			@Override
			public IControl create(final ICustomWidgetFactory widgetFactory) {
				final ICustomWidgetCreator<? extends IControl> original = controlFactory.createPropertyValidationLabel(propertyName);
				final IControl result = original.create(widgetFactory);
				valueHolder.set((IValidationResultLabel) result);
				return result;
			}
		});
	}

	private void testAllValidatorsForInitialValidPerson(final IBeanProxy<IPerson> personProxy) {

		IValidationResult validationResult = personProxy.validate();
		Assert.assertTrue(validationResult.isOk());

		//make name invalid
		final IPerson person = personProxy.getBean();
		person.setName(PersonNameValidator.FORBIDDEN_NAME);
		validationResult = personProxy.validate();
		IValidationMessage worstFirst = validationResult.getWorstFirst();
		Assert.assertEquals(personAttributes.getAttribute(IPerson.NAME_PROPERTY).getCurrentLabel(), worstFirst.getContext());
		Assert.assertEquals(MessageType.ERROR, worstFirst.getType());

		//make name valid again
		personProxy.undoModifications();
		validationResult = personProxy.validate();
		Assert.assertTrue(validationResult.isOk());

		//make name invalid
		person.setName(null);
		validationResult = personProxy.validate();
		worstFirst = validationResult.getWorstFirst();
		Assert.assertEquals(personAttributes.getAttribute(IPerson.NAME_PROPERTY).getCurrentLabel(), worstFirst.getContext());
		Assert.assertEquals(MessageType.INFO_ERROR, worstFirst.getType());

		//make name valid again
		personProxy.undoModifications();
		validationResult = personProxy.validate();
		Assert.assertTrue(validationResult.isOk());

		//make lastname invalid
		person.setLastname(null);
		validationResult = personProxy.validate();
		worstFirst = validationResult.getWorstFirst();
		Assert.assertEquals(personAttributes.getAttribute(IPerson.LAST_NAME_PROPERTY).getCurrentLabel(), worstFirst.getContext());
		Assert.assertEquals(MessageType.INFO_ERROR, worstFirst.getType());

		//make lastname valid again
		personProxy.undoModifications();
		validationResult = personProxy.validate();
		Assert.assertTrue(validationResult.isOk());

		//make name invalid again
		person.setName(PersonNameValidator.FORBIDDEN_NAME);
		validationResult = personProxy.validate();
		worstFirst = validationResult.getWorstFirst();
		Assert.assertEquals(personAttributes.getAttribute(IPerson.NAME_PROPERTY).getCurrentLabel(), worstFirst.getContext());
		Assert.assertEquals(MessageType.ERROR, worstFirst.getType());

		//modify lastname valid
		person.setLastname("Foo");

		//name must still be invalid
		validationResult = personProxy.validate();
		worstFirst = validationResult.getWorstFirst();
		Assert.assertEquals(personAttributes.getAttribute(IPerson.NAME_PROPERTY).getCurrentLabel(), worstFirst.getContext());
		Assert.assertEquals(MessageType.ERROR, worstFirst.getType());
	}

}
