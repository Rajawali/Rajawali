package rajawali.materials;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import rajawali.renderer.RajawaliRenderer;


public class MaterialManager extends AResourceManager {
	private static MaterialManager instance = null;
	private List<AMaterial> mMaterialList;
	
	private MaterialManager()
	{
		mMaterialList = Collections.synchronizedList(new CopyOnWriteArrayList<AMaterial>());
		mRenderers = Collections.synchronizedList(new CopyOnWriteArrayList<RajawaliRenderer>());
	}
	
	public static MaterialManager getInstance()
	{
		if(instance == null)
		{
			instance = new MaterialManager();
		}
		return instance;
	}
	
	public AMaterial addMaterial(AMaterial material)
	{
		if(material == null) return null;
		for(AMaterial mat : mMaterialList)
		{
			if(mat == material)
				return material;
		}
		mRenderer.queueAddTask(material);
		mMaterialList.add(material);
		return material;
	}
	
	public void taskAdd(AMaterial material)
	{
		material.setOwnerIdentity(mRenderer.getClass().toString());
		material.add();
	}
	
	public void removeMaterial(AMaterial material)
	{
		if(material == null) return;
		mRenderer.queueRemoveTask(material);
	}
	
	public void taskRemove(AMaterial material)
	{
		material.remove();
		mMaterialList.remove(material);
	}
	
	public void reload()
	{
		mRenderer.queueReloadTask(this);
	}
	
	public void taskReload()
	{
		int len = mMaterialList.size();
		for (int i = 0; i < len; i++)
		{
			AMaterial material = mMaterialList.get(i);
			material.reload();
		}
	}
	
	public void reset()
	{
		mRenderer.queueResetTask(this);
	}
	
	public void taskReset()
	{
		int count = mMaterialList.size();
		
		for(int i=0; i<count; i++)
		{
			AMaterial material = mMaterialList.get(i);
			
			if(material.getOwnerIdentity().equals(mRenderer.getClass().toString()))
			{
				material.remove();
				mMaterialList.remove(i);
				i -= 1;
				count -= 1;
							
			}			
		}
		
		if (mRenderers.size() > 0)
		{
			mRenderer = mRenderers.get(mRenderers.size() - 1);
			reload();
		} else {
			mMaterialList.clear();
		}
	}
	
	public void taskReset(RajawaliRenderer renderer)
	{
		if (renderer != mRenderer)
			return;

		taskReset();
	}
	
	public int getNumMaterials()
	{
		return mMaterialList.size();
	}
	
	public TYPE getFrameTaskType() {
		return TYPE.MATERIAL_MANAGER;
	}
}
