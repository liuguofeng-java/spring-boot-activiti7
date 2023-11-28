import { Element } from "bpmn-js/lib/model/Types";
import editor from "@/components/BpmnJs/store/editor";
import modeler from "@/components/BpmnJs/store/modeler";
import { getBusinessObject, is } from "bpmn-js/lib/util/ModelUtil";

////////// only in bpmn:StartEvent
export function getInitiatorValue(element: Element): string | undefined {
  const prefix = editor().getProcessEngine;
  const businessObject = getBusinessObject(element);

  return businessObject.get(`${prefix}:initiator`);
}
export function setInitiatorValue(element: Element, value: string | undefined) {
  const prefix = editor().getProcessEngine;
  const modeling = modeler().getModeling;
  const businessObject = getBusinessObject(element);
  modeling.updateModdleProperties(element, businessObject, {
    [`${prefix}:initiator`]: value
  });
}

export function isStartInitializable(element: BpmnElement): boolean {
  const prefix = editor().getProcessEngine;
  return is(element, `${prefix}:Initiator`) && !is(element.parent, "bpmn:SubProcess");
}

export function isUserAssignmentSupported(element: BpmnElement) {
  return is(element, `${editor().getProcessEngine}:Assignable`);
}
